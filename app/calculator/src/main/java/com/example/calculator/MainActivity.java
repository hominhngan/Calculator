package com.example.calculator;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.mariuszgromada.math.mxparser.Expression;

public class MainActivity extends AppCompatActivity {
    private TextView editTextText;
    private TextView textViewExpression;
    private Button buttonEqual;

    private boolean isNightMode = false;

    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextText = findViewById(R.id.textViewResult);
        textViewExpression = findViewById(R.id.textViewExpression);
        buttonEqual = findViewById(R.id.buttonEqual);
        databaseManager = new DatabaseManager(this);

        setButtonClickListeners();
        editTextText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                buttonEqual.setEnabled(Calculation.isExpressionComplete(s.toString()));
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonEqual.setEnabled(Calculation.isExpressionComplete(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {
                buttonEqual.setEnabled(Calculation.isExpressionComplete(s.toString()));
            }
        });
        Button btn = findViewById(R.id.switchTheme);
        btn.setOnClickListener((e) -> {
            isNightMode = !isNightMode;
            AppCompatDelegate.setDefaultNightMode(
                    isNightMode ?
                            AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });
        setUpTextWatcher();
        showLastCalculation();
    }

    private void setButtonClickListeners() {
        int[] buttonIds = {R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9, R.id.buttonDot, R.id.buttonAC, R.id.buttonAdd, R.id.buttonSubtract, R.id.buttonMultiply, R.id.buttonDivide, R.id.buttonPercent, R.id.buttonParentheses, R.id.buttonEqual, R.id.buttonBack};
        for (int buttonId : buttonIds) {
            Button button = findViewById(buttonId);
            button.setOnClickListener(view -> onButtonClick(view));
        }
    }

    private void setUpTextWatcher() {
        editTextText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                validateExpression();
                textViewExpression.setVisibility(View.GONE); // Ẩn TextView hiển thị biểu thức khi người dùng nhập liệu
            }
        });
    }

    private void validateExpression() {
        String expression = editTextText.getText().toString();
        Expression expressionEval = new Expression(expression);
        boolean isValid = expressionEval.checkSyntax();
        buttonEqual.setEnabled(isValid);
    }

    private void onButtonClick(View view) {
        Button button = (Button) view;
        String buttonText = button.getText().toString();
        switch (buttonText) {
            case "=":
                calculateResult();
                break;
            case "()":
                handleParentheses();
                break;
            case "⌫":
                removeLastInput();
                break;
            case "AC":
                clearInput();
                break;
            default:
                appendInput(buttonText);
                break;
        }
    }

    private void appendInput(String input) {
        editTextText.setText(editTextText.getText().toString() + input);
    }

    private void removeLastInput() {
        String s = editTextText.getText().toString();
        if (s.length() > 0) {
            editTextText.setText(s.substring(0, s.length() - 1));
        }
    }

    private void clearInput() {
        editTextText.setText("");
    }

    private boolean isOpenParentheses = false;

    private void handleParentheses() {
        if (isOpenParentheses) {
            appendInput(")");
            isOpenParentheses = false;
        } else {
            appendInput("(");
            isOpenParentheses = true;
        }
    }

    private void calculateResult() {
        try {
            String expression = editTextText.getText().toString();
            Expression expressionEval = new Expression(expression);
            double result = expressionEval.calculate();
            if (Double.isNaN(result)) {
                throw new Exception("Invalid expression");
            }
            editTextText.setText(String.valueOf(result));
            textViewExpression.setText(expression); // Hiển thị biểu thức khi bấm "="
            textViewExpression.setVisibility(View.VISIBLE);

            databaseManager.open();
            databaseManager.addData(expression, result);
            databaseManager.close();
        } catch (Exception e) {
            editTextText.setText("Error");
        }
    }

    private void showLastCalculation() {
        databaseManager.open();
        Calculation lastCalculation = databaseManager.getLastCalculation();
        databaseManager.close();
        if (lastCalculation != null) {
            editTextText.setText(lastCalculation.getExpression() + " = " + lastCalculation.getResult());
        }
    }
}