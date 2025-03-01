package lanse.fractalworld.FractalCalculator;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CustomFractalCalculator {

    private static final Logger LOGGER = LogManager.getLogger();

    public static String[] defaultCustomFractalFormula = {
            "zx * zx - zy * zy + x",  // Formula for zx
            "2 * zx * zy + y",       // Formula for zy
            "4"                      // Escape radius
    };

    public static String[] customFractalFormula = defaultCustomFractalFormula;
    private static List<String> zxFormulaTokens;
    private static List<String> zyFormulaTokens;
    private static List<String> escapeRadiusTokens;
    public static boolean formulaChanged = true;
    public static boolean formulaIsInvalid = false;

    public static void setFractalFormula(String zxFormula, String zyFormula, String escapeRadius) {
        customFractalFormula[0] = zxFormula;
        customFractalFormula[1] = zyFormula;
        customFractalFormula[2] = escapeRadius;
        formulaChanged = true;
    }

    public static int evaluateFractal(double x, double y) {
        double zx = 0, zy = 0, prevZx = 0, prevZy = 0;
        int iter = 0; double newZx; double newZy;

        compileFormulas();

        double escapeRadius;
        // Reuse a mutable map to avoid object creation in each iteration
        Map<String, Double> variableMap = new HashMap<>();
        variableMap.put("x", x);
        variableMap.put("y", y);

        try {
            // Precompute escape radius outside the loop
            variableMap.put("zx", zx);
            variableMap.put("zy", zy);
            variableMap.put("prevZx", prevZx);
            variableMap.put("prevZy", prevZy);

            variableMap.put("pi", Math.PI);
            variableMap.put("phi", (1 + Math.sqrt(5)) / 2);
            variableMap.put("zeta3", 1.2020569032);
            variableMap.put("gamma", 0.5772156649);
            variableMap.put("catalan", 0.9159655942);

            escapeRadius = MathExpressionEvaluator.evaluateInfix(escapeRadiusTokens, variableMap);

        } catch (Exception e) {
            LOGGER.error("Error during fractal evaluation", e);
            formulaIsInvalid = true;
            return -40404; // Error code
        }

        while (zx * zx + zy * zy < escapeRadius && iter < FractalGenerator.MAX_ITER) {
            variableMap.put("zx", zx);
            variableMap.put("zy", zy);
            variableMap.put("prevZx", prevZx);
            variableMap.put("prevZy", prevZy);

            try {
                newZx = MathExpressionEvaluator.evaluateInfix(zxFormulaTokens, variableMap);
                newZy = MathExpressionEvaluator.evaluateInfix(zyFormulaTokens, variableMap);

            } catch (Exception e) {
                LOGGER.error("Error during fractal evaluation", e);
                formulaIsInvalid = true;
                return -40404; // Error code
            }

            prevZx = zx;
            prevZy = zy;
            zx = newZx;
            zy = newZy;
            iter++;
        }
        return iter;
    }

    private static void compileFormulas() {
        if (formulaChanged) {
            try {
                // Tokenize formulas only when they change
                zxFormulaTokens = MathExpressionEvaluator.tokenize(customFractalFormula[0]);
                zyFormulaTokens = MathExpressionEvaluator.tokenize(customFractalFormula[1]);
                escapeRadiusTokens = MathExpressionEvaluator.tokenize(customFractalFormula[2]);
                formulaChanged = false; // Reset the flag
                formulaIsInvalid = false; // Ensure formula state is valid
            } catch (Exception e) {
                LOGGER.error("Error compiling fractal formulas", e);
                formulaIsInvalid = true;
            }
        }
    }
}