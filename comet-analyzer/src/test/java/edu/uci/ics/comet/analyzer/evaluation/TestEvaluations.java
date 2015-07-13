package edu.uci.ics.comet.analyzer.evaluation;

import org.junit.Assert;
import org.junit.Test;

import edu.uci.ics.comet.analyzer.query.mongodb.AbstractMongoTest;

/*
 * This class shouldn't be tested using Mongo but a TestQueryHandler.
 */
public class TestEvaluations extends AbstractMongoTest {

	private static final ExpectedEvaluation FAILED_EVAL = new ExpectedEvaluation(EvaluationResultType.FAILED);
	private static final ExpectedEvaluation ERROR_EVAL = new ExpectedEvaluation(EvaluationResultType.ERROR);
	private static final ExpectedEvaluation WARN_EVAL = new ExpectedEvaluation(EvaluationResultType.WARNING);
	private static final ExpectedEvaluation PASS_EVAL = new ExpectedEvaluation(EvaluationResultType.PASS);
	private static final String EVENT_TYPE = COMETMembers.TYPE.getName();
	private static final String ISLAND = COMETMembers.SOURCE_ISLAND.getName();

	public TestEvaluations() {
	}

	private PatternEvaluation newPattern() {
		PatternEvaluation eval = new PatternEvaluation();
		eval.setQueryHandler(getQueryHandler());
		return eval;
	}

	private Not newNot() {
		Not not = new Not();
		not.setQueryHandler(getQueryHandler());
		return not;
	}

	private Evaluation newAnd() {
		return new And().setQueryHandler(getQueryHandler());
	}

	private Evaluation newOr() {
		return new Or().setQueryHandler(getQueryHandler());
	}

	private static void assertEval(Evaluation eval, EvaluationResultType expectedResult) {
		Assert.assertEquals("Evaluation result is incorrect.", expectedResult, eval.evaluate().getResultType());
	}

	private static COMETEvent newEvent() {
		return new COMETEvent();
	}

	private static void assertEvaluationFails(Evaluation eval) {
		assertEval(eval, EvaluationResultType.FAILED);
	}

	private static void assertEvaluationWarn(Evaluation eval) {
		assertEval(eval, EvaluationResultType.WARNING);
	}

	private static void assertEvaluationPasses(Evaluation eval) {
		assertEval(eval, EvaluationResultType.PASS);
	}

	private static void assertEvaluationError(Evaluation eval) {
		assertEval(eval, EvaluationResultType.ERROR);
	}

	/*
	 * Evaluations
	 */

	/*
	 * Pattern Evaluation.
	 */

	@Test
	public void testEmptyPattern() {
		Evaluation eval = newPattern();

		assertEvaluationPasses(eval);
	}

	@Test
	public void testOneMatchPassPattern() {
		PatternEvaluation eval = newPattern();

		eval.addEvent(newEvent().put(ISLAND, "bob"));

		assertEvaluationPasses(eval);
	}

	@Test
	public void whenStartIndexIsSetThenItShouldBeConsidered() {
		PatternEvaluation eval = newPattern();
		eval.setStartEventID(10L);

		eval.addEvent(newEvent().put(ISLAND, "bob"));

		assertEvaluationFails(eval);
	}

	@Test
	public void testMultiMatchPassPattern() {
		PatternEvaluation eval = newPattern();

		eval.addEvent(newEvent().put(ISLAND, "alice"));
		eval.addEvent(newEvent().put(ISLAND, "bob"));
		eval.addEvent(newEvent().put(ISLAND, "alice"));

		assertEvaluationPasses(eval);
	}

	@Test
	public void testOneMatchFailPattern() {
		PatternEvaluation eval = newPattern();

		eval.addEvent(newEvent().put(ISLAND, "carol"));

		assertEvaluationFails(eval);
	}

	@Test
	public void testMultiMatchFailPattern() {
		PatternEvaluation eval = newPattern();

		eval.addEvent(newEvent().put(ISLAND, "bob"));
		eval.addEvent(newEvent().put(ISLAND, "bob"));

		assertEvaluationFails(eval);
	}

	@Test
	public void testMultiComplexMatchPassPattern() {
		PatternEvaluation eval = newPattern();

		eval.addEvent(newEvent().put(ISLAND, "alice").put(EVENT_TYPE, "curl-new"));
		eval.addEvent(newEvent().put(ISLAND, "bob").put(EVENT_TYPE, "curl-new"));
		eval.addEvent(newEvent().put(ISLAND, "alice").put(EVENT_TYPE, "curl-send"));

		assertEvaluationPasses(eval);
	}

	@Test
	public void testMultiComplexMatchFailPattern() {
		PatternEvaluation eval = newPattern();

		eval.addEvent(newEvent().put(ISLAND, "alice").put(EVENT_TYPE, "curl-new"));
		eval.addEvent(newEvent().put(ISLAND, "bob").put(EVENT_TYPE, "curl-send"));
		eval.addEvent(newEvent().put(ISLAND, "alice").put(EVENT_TYPE, "curl-send"));

		assertEvaluationFails(eval);
	}

	private static void nestEvals(Evaluation composite, Evaluation... evals) {
		for (Evaluation evaluation : evals) {
			composite.addNestedEvaluation(evaluation);
		}
	}

	private static void assertEvalWith(Evaluation composite, EvaluationResultType expected, Evaluation... nestedEvals) {
		nestEvals(composite, nestedEvals);
		assertEval(composite, expected);
	}

	/*
	 * AND Evaluation.
	 */

	@Test
	public void whenAllEvalsPassThenANDEvalShouldPass() {
		assertEvalWith(newAnd(), EvaluationResultType.PASS, PASS_EVAL, PASS_EVAL);
	}

	@Test
	public void whenOneEvalFailsAndNoErrorsThenANDEvalShouldFail() {
		assertEvalWith(newAnd(), EvaluationResultType.FAILED, PASS_EVAL, WARN_EVAL, FAILED_EVAL);
	}

	@Test
	public void whenOneErrorThenAndEvalShouldError() {
		assertEvalWith(newAnd(), EvaluationResultType.ERROR, PASS_EVAL, WARN_EVAL, ERROR_EVAL, FAILED_EVAL);
	}

	/*
	 * OR Evaluation
	 */
	@Test
	public void whenAllEvalsPassThenOREvalShouldPass() {
		assertEvalWith(newOr(), EvaluationResultType.PASS, PASS_EVAL, PASS_EVAL);
	}

	@Test
	public void whenOneEvalPassesAndNoErrorsThenOREvalShouldFail() {
		assertEvalWith(newOr(), EvaluationResultType.PASS, PASS_EVAL, WARN_EVAL, FAILED_EVAL);
	}

	/*
	 * NOT Evaluation
	 */
	@Test
	public void whenMoreThanOneEvalIsNestedToNOTThenItShouldThrowRuntimeException() {
		try {
			nestEvals(newNot(), newPattern(), newPattern());
			Assert.fail("Nesting more than one element should have thrown RuntimeException.");
		} catch (RuntimeException e) {
			// Code is OK.
		}
	}

	@Test
	public void whenPassIsObtainedItShouldBeChangedToFail() {
		assertEvalWith(newNot(), EvaluationResultType.FAILED, PASS_EVAL);
	}

	@Test
	public void whenFailIsObtainedItShouldBeChangedToPass() {
		assertEvalWith(newNot(), EvaluationResultType.PASS, FAILED_EVAL);
	}

	@Test
	public void whenWarnIsObtainedItShouldBeReturnedAsIs() {
		assertEvalWith(newNot(), EvaluationResultType.WARNING, WARN_EVAL);
	}

	@Test
	public void whenErrorOccursAndItIsNotRedefinedThenErrorShouldBeReturned() {
		assertEvalWith(newNot(), EvaluationResultType.ERROR, ERROR_EVAL);
	}

	@Test
	public void whenErrorOccursAndItIsRedefinedThenTheConfiguredShouldBeReturned() {
		// Warning is returned instead of Error.
		assertEvalWith(newNot().setOnErrorSeverity(EvaluationResultType.WARNING), EvaluationResultType.WARNING, ERROR_EVAL);
	}

	/*
	 * Configured severity.
	 */
	@Test
	public void whenSeverityIsConfiguredThenItShouldReplaceTheNaturalOne() {
		PatternEvaluation eval = newPattern();
		/*
		 * If an unexpected result is obtained, it should be replaced by
		 * warning.
		 */
		eval.setConfiguredSeverity(EvaluationResultType.WARNING);

		eval.addEvent(newEvent().put(ISLAND, "carol"));

		assertEvaluationWarn(eval);
	}
}