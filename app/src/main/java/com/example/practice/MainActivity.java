package com.example.practice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    TextView tvInput;
    Button btnOn;
    StringBuilder inputBuilder = new StringBuilder();
    boolean isCalculatorOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvInput = findViewById(R.id.tvInput);
        btnOn = findViewById(R.id.btnOn);

        if (savedInstanceState != null) {
            isCalculatorOn = savedInstanceState.getBoolean("isCalculatorOn", false);
            String savedInput = savedInstanceState.getString("input", "");
            inputBuilder.append(savedInput);
        }

        // Initialize display and button state based on the current calculator state
        if (isCalculatorOn) {
            btnOn.setText("OFF");
            if (inputBuilder.length() == 0) {
                tvInput.setText("SHIVARADDI");
            } else {
                tvInput.setText(inputBuilder.toString());
            }
        } else {
            btnOn.setText("ON");
            tvInput.setText("");
        }

        setButtonListeners();
        setCalculatorState(isCalculatorOn);

        btnOn.setOnClickListener(v -> {
            isCalculatorOn = !isCalculatorOn;
            if (isCalculatorOn) {
                // When turning on, clear old input and display "SHIVARADDI"
                inputBuilder.setLength(0);
                tvInput.setText("SHIVARADDI");
                btnOn.setText("OFF");
            } else {
                // When turning off, clear everything and set button text
                tvInput.setText("");
                inputBuilder.setLength(0);
                btnOn.setText("ON");
            }
            setCalculatorState(isCalculatorOn);
        });
    }

    private void setButtonListeners() {
        int[] numberIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnDot, R.id.btn00};
        int[] operatorIds = {R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide};

        View.OnClickListener numberListener = v -> {
            if (isCalculatorOn) {
                // If "SHIVARADDI" is showing, clear it before appending numbers
                if (tvInput.getText().toString().equals("SHIVARADDI")) {
                    tvInput.setText("");
                }
                Button b = (Button) v;
                inputBuilder.append(b.getText().toString());
                tvInput.setText(inputBuilder.toString());
            }
        };

        for (int id : numberIds) {
            findViewById(id).setOnClickListener(numberListener);
        }

        View.OnClickListener operatorListener = v -> {
            if (isCalculatorOn && inputBuilder.length() > 0) {
                Button b = (Button) v;
                if (!isOperator(inputBuilder.charAt(inputBuilder.length() - 1))) {
                    inputBuilder.append(" ").append(b.getText().toString()).append(" ");
                    tvInput.setText(inputBuilder.toString());
                }
            }
        };

        for (int id : operatorIds) {
            findViewById(id).setOnClickListener(operatorListener);
        }

        findViewById(R.id.btnClear).setOnClickListener(v -> {
            if (isCalculatorOn) {
                inputBuilder.setLength(0);
                tvInput.setText("");
            }
        });

        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            if (isCalculatorOn && inputBuilder.length() > 0) {
                if (inputBuilder.charAt(inputBuilder.length() - 1) == ' ') {
                    int lastOperatorIndex = inputBuilder.lastIndexOf(" ");
                    inputBuilder.setLength(lastOperatorIndex);
                } else {
                    inputBuilder.setLength(inputBuilder.length() - 1);
                }
                tvInput.setText(inputBuilder.toString());
            }
        });

        findViewById(R.id.btnEqual).setOnClickListener(v -> {
            if (isCalculatorOn) {
                try {
                    String expr = inputBuilder.toString().replace("×", "*").replace("÷", "/").replace("−", "-");
                    double result = evaluateExpression(expr);
                    String formattedResult = formatResult(result);
                    tvInput.setText(formattedResult);
                    inputBuilder.setLength(0);
                    inputBuilder.append(formattedResult);
                } catch (Exception e) {
                    tvInput.setText("Error");
                    inputBuilder.setLength(0);
                }
            }
        });
    }

    private void setCalculatorState(boolean enabled) {
        findViewById(R.id.btnClear).setEnabled(enabled);
        findViewById(R.id.btnDelete).setEnabled(enabled);
        findViewById(R.id.btnEqual).setEnabled(enabled);

        int[] allButtons = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6,
                R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnDot, R.id.btn00, R.id.btnPlus,
                R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide};

        for (int id : allButtons) {
            findViewById(id).setEnabled(enabled);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("input", inputBuilder.toString());
        outState.putBoolean("isCalculatorOn", isCalculatorOn);
    }

    // Evaluate math expression using Shunting-yard algorithm
    private double evaluateExpression(String expr) {
        char[] tokens = expr.toCharArray();
        Stack<Double> values = new Stack<>();
        Stack<Character> ops = new Stack<>();

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i] == ' ') continue;

            if (tokens[i] >= '0' && tokens[i] <= '9' || tokens[i] == '.') {
                StringBuilder sbuf = new StringBuilder();
                while (i < tokens.length && (tokens[i] >= '0' && tokens[i] <= '9' || tokens[i] == '.')) {
                    sbuf.append(tokens[i++]);
                }
                values.push(Double.parseDouble(sbuf.toString()));
                i--;
            } else if (tokens[i] == '+' || tokens[i] == '-' || tokens[i] == '*' || tokens[i] == '/') {
                while (!ops.empty() && hasPrecedence(tokens[i], ops.peek())) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.push(tokens[i]);
            }
        }

        while (!ops.empty()) {
            values.push(applyOp(ops.pop(), values.pop(), values.pop()));
        }

        return values.pop();
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '−' || c == '×' || c == '÷' || c == '*' || c == '/';
    }

    private String formatResult(double result) {
        if (result == (long) result) {
            return String.valueOf((long) result);
        } else {
            return String.valueOf(result);
        }
    }

    private boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') return false;
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) return false;
        return true;
    }

    private double applyOp(char op, double b, double a) {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/':
                if (b == 0) throw new UnsupportedOperationException("Cannot divide by zero");
                return a / b;
        }
        return 0;
    }
}