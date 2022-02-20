import java.util.ArrayList;

public class ParseMath {
    public static final String VALID_NUMBERS = "1234567890.";
    public static final String VALID_OPS = "^*/+-%";
    public static final String[] VALID_FUNCTIONS = {"sin", "sin\u207b¹", "cos", "cos\u207b¹", "tan", "tan\u207b¹", "abs", "sqrt", "ln", "log\u2081\u2080", "fac"};
    public static final String[] PEMDAS =  {"^", "*/%", "+-"};

    // finds the close parenthesis and returns the whole block
    private static String getBlockAt(int index, String text) {
        int closeIndex = index + 1;
        int counter = 1;
        while (counter != 0 && closeIndex < text.length()) {
            if (("" + text.charAt(closeIndex)).equals("(")) {
                counter += 1;
            } else if (("" + text.charAt(closeIndex)).equals(")")) {
                counter -= 1;
            }
            closeIndex += 1;
        }
        return text.substring(index, closeIndex);
    }

    public static String formatFormula(String formula) throws Exception {
        String newFormula = "";
        for (int i = 0; i < formula.length(); i++) {
            String character = "" + formula.charAt(i);
            // -sin(2+1) -> -1*sin(2+1)
            if ((character.equals("(") || isFunction(i, formula)) && newFormula.length() > 0 && ("" + newFormula.charAt(newFormula.length() - 1)).equals("-")) {
                newFormula += "1*";
            } if ((character.equals("(") || isFunction(i, formula)) && newFormula.length() > 0 && VALID_NUMBERS.contains("" + newFormula.charAt(newFormula.length() - 1))) {
                newFormula += "*";
            } if (VALID_NUMBERS.contains(character) || VALID_OPS.contains(character) || character.equals("(") || character.equals(")")) {
                newFormula += character;
            } else if (isFunction(i, formula)) {
                String function = getFunction(i, formula);
                newFormula += function;
                i += function.length() - 1;
            } else if (character.equals("π")) {
                // can't have 3pi, for example
                if (VALID_NUMBERS.contains("" + newFormula.charAt(newFormula.length() - 1))) {
                    throw new Exception();
                }
                newFormula += "" + Math.PI;
            } else if (character.equals("e")) {
                if (VALID_NUMBERS.contains("" + newFormula.charAt(newFormula.length() - 1))) {
                    throw new Exception();
                }
                newFormula += "" + Math.E;
            }
        }
        return newFormula;
    }

    public static boolean isFunction(int index, String context) {
        String sub = context.substring(index);
        for (String function : VALID_FUNCTIONS) {
            if (sub.startsWith(function)) {
                return true;
            }
        }
        return false;
    }

    public static String getFunction(int index, String context) {
        String sub = context.substring(index);
        String longestMatchingFunction = "";
        for (String function : VALID_FUNCTIONS) {
            if (function.length() > longestMatchingFunction.length() && sub.startsWith(function)) {
                longestMatchingFunction = function;
            }
        }
        return longestMatchingFunction;
    }

    // recursive
    public static ArrayList<Object> parseFormula(String formula, boolean isRadians) throws Exception {
        formula = formatFormula(formula);
        ArrayList<Object> parsedFormula = new ArrayList<Object>();

        // to do negatives, assume the formula goes back and forth between objects and operations
        // that way, "-" can be both a valid character and a vaild 'number'
        boolean isOp = false;
        
        int index = 0;
        while (index < formula.length()) {
            String character = "" + formula.charAt(index);
            // recurse on parentheses
            if (character.equals("(")) {
                String block = getBlockAt(index, formula);
                index += block.length();
                parsedFormula.add(parseFormula(block.substring(1, block.length() - 1), isRadians));
                isOp = true;
            } else {
                if (!isOp) {
                    if (isFunction(index, formula)) {
                        // reduce whole function to a constant
                        String functionInput = getBlockAt(formula.indexOf("(", index + 1), formula);
                        double simplifiedInput = solve(parseFormula(functionInput.substring(1, functionInput.length() - 1), isRadians));
                        String function = getFunction(index, formula);
                        double output = 0.0;
                        if (function.equals("sin") || function.equals("cos") || function.equals("tan")) {
                            // use the right mode
                            if (!isRadians) {
                                simplifiedInput = Math.toRadians(simplifiedInput);
                            }
                            if (function.equals("sin")) {
                                output = Math.sin(simplifiedInput);
                            } else if (function.equals("cos")) {
                                output = Math.cos(simplifiedInput);
                            } else if (function.equals("tan")) {
                                output = Math.tan(simplifiedInput);
                            }
                            
                        } else if (function.equals("sin\u207b¹") || function.equals("cos\u207b¹") || function.equals("tan\u207b¹")) {
                            if (function.equals("sin\u207b¹")) {
                                output = Math.asin(simplifiedInput);
                            } else if (function.equals("cos\u207b¹")) {
                                output = Math.acos(simplifiedInput);
                            } else if (function.equals("tan\u207b¹")) {
                                output = Math.atan(simplifiedInput);
                            }
                            if (!isRadians) {
                                output = Math.toDegrees(output);
                            }
                        } else if (function.equals("sqrt")) {
                            output = Math.sqrt(simplifiedInput);
                        } else if (function.equals("abs")) {
                            output = Math.abs(simplifiedInput);
                        } else if (function.equals("ln")) {
                            output = Math.log(simplifiedInput);
                        } else if (function.equals("log\u2081\u2080")) {
                            output = Math.log10(simplifiedInput);
                        } else if (function.equals("fac")) {
                            // factorials
                            int input = (int) simplifiedInput;
                            double total = 1;
                            for (int x = input; input > 0; input--) {
                                total *= input;
                            }
                            output = total;
                        }
                        parsedFormula.add(output);
                        index += functionInput.length() + function.length();
                    }
                    else {
                        // constants
                        String thing = "";
                        while ((VALID_NUMBERS.contains(character) || (character.equals("-") && thing.length() == 0)) && index < formula.length()) {
                            thing += character;
                            index += 1;
                            try {
                                character = "" + formula.charAt(index);
                            } catch (Exception e) {
                                character = "";
                            }
                        }
                        parsedFormula.add(Double.parseDouble(thing));
                    }
                    isOp = true;
                } else {
                    // operations
                    parsedFormula.add(character);
                    index += 1;
                    isOp = false;
                }
            }
        }
        return parsedFormula;
    }

    // also recursive
    public static double solve(ArrayList<Object> formula) throws Exception {
        for (int index = 0; index < formula.size(); index++) {
            Object object = formula.get(index);
            if (object instanceof ArrayList) {
                // recurse on parentheses
                formula.set(index, solve((ArrayList<Object>) object));
            }
        }

        for (String currentOp : PEMDAS) {
            // skip over operations and get constants
            for (int index = 0; index < formula.size() - 2; index += 2) {
                String op = "" + formula.get(index + 1);
                if (currentOp.contains(op)) {
                    // reduce 3 + 2 to 5 in the formula
                    double first = (double) formula.get(index);
                    double second = (double) formula.get(index + 2);
                    double result = 0.0;
                    if (op.equals("+")) {
                        result = first + second;
                    } else if (op.equals("-")) {
                        result = first - second;
                    } else if (op.equals("*")) {
                        result = first * second;
                    } else if (op.equals("/")) {
                        result = first / second;
                    } else if (op.equals("%")) {
                        result = first % second;
                    } else if (op.equals("^")) {
                        result = Math.pow(first, second);
                    }
                    ArrayList<Object> newFormula = new ArrayList<Object>();
                    for (int preIndex = 0; preIndex < index; preIndex++) {
                        newFormula.add(formula.get(preIndex));
                    }
                    newFormula.add(result);
                    for (int postIndex = index + 3; postIndex < formula.size(); postIndex++) {
                        newFormula.add(formula.get(postIndex));
                    }
                    formula = newFormula;
                    index -= 2;
                }
            }
        }
        return (double) formula.get(0);
    }
}