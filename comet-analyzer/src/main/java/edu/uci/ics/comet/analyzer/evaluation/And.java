/**
 * 
 */
package edu.uci.ics.comet.analyzer.evaluation;

/**
 * @author matias
 *
 */
public class And extends Evaluation {

	/**
	 * 
	 */
	public And() {
	}

	@Override
	protected EvaluationResult doTheEvaluation() {
		/*
		 * The following algorithm could be abstracted away here and in Or and
		 * solved using an priority-based list.
		 */
		EvaluationResultType result = EvaluationResultType.PASS;

		for (Evaluation eval : this.getNestedEvaluations()) {
			EvaluationResult nestedResult = eval.evaluate();

			if (nestedResult.getResultType().equals(EvaluationResultType.ERROR)) {
				return new EvaluationResult(EvaluationResultType.ERROR);
			} else if (nestedResult.getResultType().equals(EvaluationResultType.FAILED) && !result.equals(EvaluationResultType.ERROR)) {
				result = EvaluationResultType.FAILED;
			} else if (nestedResult.getResultType().equals(EvaluationResultType.WARNING) && !result.equals(EvaluationResultType.ERROR) && !result.equals(EvaluationResultType.FAILED)) {
				result = EvaluationResultType.WARNING;
			}
		}

		return new EvaluationResult(result);
	}

	@Override
	public String toString() {
		return "AND";
	}
}