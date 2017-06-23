package edu.scalanus.compiler

import edu.scalanus.errors.{ScalanusCompileException, ScalanusException}
import edu.scalanus.ir._
import edu.scalanus.parser.ScalanusBaseVisitor
import edu.scalanus.parser.ScalanusParser._
import edu.scalanus.util.LcfPosition
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.RuleNode

import scala.collection.JavaConverters._

class CompilerVisitor(private val errors: ScalanusErrorListener) extends ScalanusBaseVisitor[Option[IrNode]] {

  //
  // Compile "monad"
  //

  private def compile[T <: IrNode](ctx: ParserRuleContext)(f: => (IrCtx) => T): Option[T] = compileM(ctx) {
    Some(f)
  }

  private def compileM[T <: IrNode](ctx: ParserRuleContext)(f: => Option[(IrCtx) => T]): Option[T] =
    try {
      val node = f.map(_.apply(IrCtx(ctx)))
      if (!errors.hasErrors) {
        Some(node.get)
      } else {
        None
      }
    } catch {
      case ex: ScalanusException =>
        if (ex.position == null) ex.position = LcfPosition(ctx)
        errors.report(ex)
        None
    }

  private def accept[T <: IrNode](ctx: ParserRuleContext): Option[T] =
    ctx.accept(this).map(_.asInstanceOf[T])

  private def acceptOptional[T <: IrNode](ctx: ParserRuleContext): Option[Option[T]] =
    if (ctx == null) {
      Some(None)
    } else {
      ctx.accept(this).map { n => Some(n.asInstanceOf[T]) }
    }


  //
  // General AST nodes
  //

  override def visitAssignStmt(ctx: AssignStmtContext): Option[IrNode] = compileM(ctx) {
    for {
      pattern <- accept[IrPattern](ctx.pattern)
      expr <- accept[IrExpr](ctx.expr)
    } yield IrAssignStmt(pattern, expr)
  }

  override def visitBreakExpr(ctx: BreakExprContext): Option[IrNode] = compile(ctx) {
    IrBreak()
  }

  override def visitBlock(ctx: BlockContext): Option[IrNode] = compile(ctx) {
    IrBlock(
      ctx.stmts.stmt.asScala
        .flatMap(accept[IrStmt])
        .toIndexedSeq
    )
  }

  override def visitBlockExpr(ctx: BlockExprContext): Option[IrNode] = accept(ctx.block)

  override def visitContinueExpr(ctx: ContinueExprContext): Option[IrNode] = compile(ctx) {
    IrContinue()
  }

  override def visitDict(ctx: DictContext): Option[IrNode] = compile(ctx) {
    IrDict(
      ctx.dictElem.asScala
        .flatMap(accept[IrDictElem])
        .toIndexedSeq
    )
  }

  override def visitDictElem(ctx: DictElemContext): Option[IrNode] = compileM(ctx) {
    for {
      key <- accept[IrExpr](ctx.expr(0))
      value <- accept[IrExpr](ctx.expr(1))
    } yield IrDictElem(key, value)
  }

  override def visitDictExpr(ctx: DictExprContext): Option[IrNode] = accept(ctx.dict)

  override def visitExprStmt(ctx: ExprStmtContext): Option[IrNode] = accept(ctx.expr)

  override def visitFnCallExpr(ctx: FnCallExprContext): Option[IrFnCallExpr] = compileM(ctx) {
    for {
      fnExpr <- accept[IrExpr](ctx.expr)
    } yield {
      val args = Option(ctx.fnCallArgs).map {
        _.expr.asScala
          .flatMap(accept[IrExpr])
          .toIndexedSeq
      }.getOrElse {
        IndexedSeq.empty
      }

      IrFnCallExpr(fnExpr, args)
    }
  }

  override def visitFnItem(ctx: FnItemContext): Option[IrFnItem] = compileM(ctx) {
    for {
      params <- acceptOptional[IrPattern](ctx.pattern)
      routine <- accept[IrExpr](ctx.block)
    } yield {
      val name = ctx.IDENT.getText
      IrFnItem(name, params, routine)
    }
  }

  override def visitForExpr(ctx: ForExprContext): Option[IrForExpr] = compileM(ctx) {
    val loop = ctx.forLoop
    for {
      pattern <- accept[IrPattern](loop.pattern)
      producer <- accept[IrExpr](loop.expr)
      routine <- accept[IrExpr](loop.block)
    } yield IrForExpr(pattern, producer, routine)
  }

  override def visitIdxAccExpr(ctx: IdxAccExprContext): Option[IrRefExpr] = compileM(ctx) {
    for {
      recv <- accept[IrExpr](ctx.expr(0))
      idx <- accept[IrExpr](ctx.expr(1))
      idxAcc <- compile(ctx) {
        IrIdxAcc(recv, idx)
      }
    } yield IrRefExpr(idxAcc)
  }

  override def visitIdxAccPattern(ctx: IdxAccPatternContext): Option[IrNode] = compileM(ctx) {
    for {
      recv <- accept[IrExpr](ctx.expr(0))
      idx <- accept[IrExpr](ctx.expr(1))
      idxAcc <- compile(ctx) {
        IrIdxAcc(recv, idx)
      }
    } yield IrRefPattern(idxAcc)
  }

  override def visitElseTail(ctx: ElseTailContext): Option[IrNode] =
    if (ctx.ifCond != null) {
      accept(ctx.ifCond)
    } else {
      accept(ctx.block)
    }

  override def visitIfCond(ctx: IfCondContext): Option[IrNode] = compileM(ctx) {
    for {
      cond <- accept[IrExpr](ctx.expr)
      ifBranch <- accept[IrExpr](ctx.block)
      elseBranch <- acceptOptional[IrExpr](ctx.elseTail)
    } yield IrIfExpr(cond, ifBranch, elseBranch)
  }

  override def visitIfExpr(ctx: IfExprContext): Option[IrNode] = accept(ctx.ifCond)

  override def visitItemStmt(ctx: ItemStmtContext): Option[IrNode] = accept(ctx.item)

  override def visitLiteral(ctx: LiteralContext): Option[IrNode] = compile(ctx) {
    IrValue(LiteralParser.parse(ctx))
  }

  override def visitLiteralExpr(ctx: LiteralExprContext): Option[IrNode] = accept(ctx.literal)

  override def visitLoopExpr(ctx: LoopExprContext): Option[IrNode] = compileM(ctx) {
    for (routine <- accept[IrExpr](ctx.loop.block)) yield IrLoopExpr(routine)
  }

  override def visitMemAccExpr(ctx: MemAccExprContext): Option[IrRefExpr] = compileM(ctx) {
    for {
      recv <- accept[IrExpr](ctx.expr)
      memAcc <- compile(ctx) {
        IrMemAcc(recv, ctx.IDENT.getText)
      }
    } yield IrRefExpr(memAcc)
  }

  override def visitMemAccPattern(ctx: MemAccPatternContext): Option[IrNode] = compileM(ctx) {
    for {
      recv <- accept[IrExpr](ctx.expr)
      memAcc <- compile(ctx) {
        IrMemAcc(recv, ctx.IDENT.getText)
      }
    } yield IrRefPattern(memAcc)
  }

  override def visitParenExpr(ctx: ParenExprContext): Option[IrNode] = accept(ctx.expr)

  override def visitPath(ctx: PathContext): Option[IrNode] = compile(ctx) {
    IrPath(ctx.IDENT.getText)
  }

  override def visitPathExpr(ctx: PathExprContext): Option[IrNode] = compileM(ctx) {
    for (path <- accept[IrRef](ctx.path)) yield IrRefExpr(path)
  }

  override def visitPathPattern(ctx: PathPatternContext): Option[IrNode] = compileM(ctx) {
    for (path <- accept[IrRef](ctx.path)) yield IrRefPattern(path)
  }

  override def visitPattern(ctx: PatternContext): Option[IrNode] = compile(ctx) {
    IrPattern(
      ctx.simplePattern.asScala
        .flatMap(accept[IrSimplePattern])
        .toIndexedSeq
    )
  }

  override def visitProgram(ctx: ProgramContext): Option[IrNode] = compile(ctx) {
    IrProgram(
      ctx.stmts.stmt.asScala
        .flatMap(accept[IrStmt])
        .toIndexedSeq
    )
  }

  override def visitReturnExpr(ctx: ReturnExprContext): Option[IrNode] = compileM(ctx) {
    for (value <- accept[IrExpr](ctx.expr)) yield IrReturn(value)
  }

  override def visitTuple(ctx: TupleContext): Option[IrNode] = compile(ctx) {
    IrTuple(
      ctx.expr.asScala
        .flatMap(accept[IrExpr])
        .toIndexedSeq
    )
  }

  override def visitTupleExpr(ctx: TupleExprContext): Option[IrNode] = accept(ctx.tuple)

  override def visitWildcardPattern(ctx: WildcardPatternContext): Option[IrNode] = compile(ctx) {
    IrWildcardPattern()
  }

  override def visitWhileExpr(ctx: WhileExprContext): Option[IrNode] = compileM(ctx) {
    val loop = ctx.whileLoop
    for {
      cond <- accept[IrExpr](loop.expr)
      routine <- accept[IrExpr](loop.block)
    } yield IrWhileExpr(cond, routine)
  }

  override def visitValuePattern(ctx: ValuePatternContext): Option[IrNode] = compileM(ctx) {
    for (expr <- accept[IrExpr](ctx.expr)) yield IrValuePattern(expr)
  }


  //
  // Unary expressions
  //

  override def visitBnotExpr(ctx: BnotExprContext): Option[IrNode] = compileUnary(IrBNotOp, ctx)

  override def visitNotExpr(ctx: NotExprContext): Option[IrNode] = compileUnary(IrNotOp, ctx)

  override def visitUnaryMinusExpr(ctx: UnaryMinusExprContext): Option[IrNode] = compileUnary(IrMinusOp, ctx)

  override def visitUnaryPlusExpr(ctx: UnaryPlusExprContext): Option[IrNode] = compileUnary(IrPlusOp, ctx)

  private def compileUnary(op: IrUnaryOp, ctx: ExprContext): Option[IrUnaryExpr] = compileM(ctx) {
    val exprCtx = ctx.getRuleContext[ExprContext](classOf[ExprContext], 0)
    for (expr <- accept[IrExpr](exprCtx)) yield IrUnaryExpr(op, expr)
  }


  //
  // Increments/decrements
  //

  override def visitPostfixDecrExpr(ctx: PostfixDecrExprContext): Option[IrNode] = compileIncr(IrPostfixDecrOp, ctx)

  override def visitPostfixIncrExpr(ctx: PostfixIncrExprContext): Option[IrNode] = compileIncr(IrPostfixIncrOp, ctx)

  override def visitPrefixDecrExpr(ctx: PrefixDecrExprContext): Option[IrNode] = compileIncr(IrPrefixDecrOp, ctx)

  override def visitPrefixIncrExpr(ctx: PrefixIncrExprContext): Option[IrNode] = compileIncr(IrPrefixIncrOp, ctx)

  private def compileIncr(op: IrIncrOp, ctx: ExprContext): Option[IrIncrExpr] = compileM(ctx) {
    val exprCtx = ctx.getRuleContext[ExprContext](classOf[ExprContext], 0)
    for (expr <- accept[IrExpr](exprCtx))
      yield expr match {
        case refExpr: IrRefExpr => IrIncrExpr(op, refExpr)
        case _ => return ?!("expected reference expression", ctx)
      }
  }


  //
  // Binary expressions
  //

  override def visitAddExpr(ctx: AddExprContext): Option[IrNode] = compileBinary(IrAddOp, ctx)

  override def visitAndExpr(ctx: AndExprContext): Option[IrNode] = compileBinary(IrAndOp, ctx)

  override def visitBandExpr(ctx: BandExprContext): Option[IrNode] = compileBinary(IrBandOp, ctx)

  override def visitBitshiftLeftExpr(ctx: BitshiftLeftExprContext): Option[IrNode] = compileBinary(IrBitshiftLeftOp, ctx)

  override def visitBitshiftRightExpr(ctx: BitshiftRightExprContext): Option[IrNode] = compileBinary(IrBitshiftRightOp, ctx)

  override def visitBorExpr(ctx: BorExprContext): Option[IrNode] = compileBinary(IrBorOp, ctx)

  override def visitDivExpr(ctx: DivExprContext): Option[IrNode] = compileBinary(IrDivOp, ctx)

  override def visitEqExpr(ctx: EqExprContext): Option[IrNode] = compileBinary(IrEqOp, ctx)

  override def visitGteqExpr(ctx: GteqExprContext): Option[IrNode] = compileBinary(IrGteqOp, ctx)

  override def visitGtExpr(ctx: GtExprContext): Option[IrNode] = compileBinary(IrGtOp, ctx)

  override def visitLteqExpr(ctx: LteqExprContext): Option[IrNode] = compileBinary(IrLteqOp, ctx)

  override def visitLtExpr(ctx: LtExprContext): Option[IrNode] = compileBinary(IrLtOp, ctx)

  override def visitModExpr(ctx: ModExprContext): Option[IrNode] = compileBinary(IrModOp, ctx)

  override def visitMulExpr(ctx: MulExprContext): Option[IrNode] = compileBinary(IrMulOp, ctx)

  override def visitNeqExpr(ctx: NeqExprContext): Option[IrNode] = compileBinary(IrNeqOp, ctx)

  override def visitOrExpr(ctx: OrExprContext): Option[IrNode] = compileBinary(IrOrOp, ctx)

  override def visitPowExpr(ctx: PowExprContext): Option[IrNode] = compileBinary(IrPowOp, ctx)

  override def visitSubExpr(ctx: SubExprContext): Option[IrNode] = compileBinary(IrSubOp, ctx)

  override def visitXorExpr(ctx: XorExprContext): Option[IrNode] = compileBinary(IrXorOp, ctx)

  def compileBinary(op: IrBinaryOp, ctx: ExprContext): Option[IrBinaryExpr] = compileM(ctx) {
    val leftCtx = ctx.getRuleContext[ExprContext](classOf[ExprContext], 0)
    val rightCtx = ctx.getRuleContext[ExprContext](classOf[ExprContext], 1)
    for {
      left <- accept[IrExpr](leftCtx)
      right <- accept[IrExpr](rightCtx)
    } yield IrBinaryExpr(op, left, right)
  }


  //
  // Unused visitor methods
  //

  override def visitFnCallArgs(ctx: FnCallArgsContext): Option[IrNode] = unreachable(ctx)

  override def visitForLoop(ctx: ForLoopContext): Option[IrNode] = unreachable(ctx)

  override def visitLoop(ctx: LoopContext): Option[IrNode] = unreachable(ctx)

  override def visitStmts(ctx: StmtsContext): Option[IrNode] = unreachable(ctx)

  override def visitUnit(ctx: UnitContext): Option[IrNode] = unreachable(ctx)

  override def visitWhileLoop(ctx: WhileLoopContext): Option[IrNode] = unreachable(ctx)

  private def unreachable(ctx: ParserRuleContext): Option[IrNode] =
    ?!(s"ICE: unused AST visitor method ${ctx.getClass.getSimpleName.stripSuffix("Context")}", ctx)


  //
  // Utilities
  //

  /** Overriden to automatically throw not implemented ICE */
  override def visitChildren(node: RuleNode): Option[IrNode] = node.getRuleContext match {
    case ctx: ParserRuleContext =>
      ?!(s"ICE: not implemented yet, ${ctx.getClass.getSimpleName.stripSuffix("Context")}", ctx)
    case _ => super.visitChildren(node)
  }

  private def ?![T <: IrNode](message: String, ctx: ParserRuleContext): Option[T] = {
    errors.report(new ScalanusCompileException(message, ctx))
    None
  }

}
