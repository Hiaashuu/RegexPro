package com.regex.modd3r;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Build;
import android.graphics.Color;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText smaliInput;
    private EditText regexOutput;
    private ClipboardManager clipboardManager;
    private Handler handler = new Handler();
    private Runnable longPressRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.parseColor("#121212")); // Status bar color set to #121212
        }

        // Display a toast message when the app starts
        Toast.makeText(this, "MODD3R", Toast.LENGTH_SHORT).show();

        smaliInput = findViewById(R.id.smaliInput);
        regexOutput = findViewById(R.id.regexOutput);
        Button pasteClipboardButton = findViewById(R.id.pasteClipboardButton);
        Button clearScreenButton = findViewById(R.id.clearScreenButton);
        Button copyRegexButton = findViewById(R.id.copyRegexButton);

        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        pasteClipboardButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pasteFromClipboard();
				}
			});

        clearScreenButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					clearScreen();
				}
			});

        copyRegexButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					copyToClipboard(regexOutput.getText().toString());
				}
			});

        // Add a TextWatcher to update the regex in real time
        smaliInput.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					// No action needed here
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					// Update regex output as the text changes
					String input = s.toString();
					String regex = generateRegex(input);
					regexOutput.setText(regex);
				}

				@Override
				public void afterTextChanged(Editable s) {
					// No action needed here
				}
			});

        // Add a TextWatcher to the regexOutput to auto-scroll to the end
        regexOutput.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					// No action needed here
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					// No action needed here
				}

				@Override
				public void afterTextChanged(Editable s) {
					regexOutput.setSelection(regexOutput.getText().length());
				}
			});

        // Show the keyboard on app start
        smaliInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.showSoftInput(smaliInput, InputMethodManager.SHOW_IMPLICIT);

        // Add touch listener to the clearScreenButton to copy input text to clipboard after 2 seconds
        clearScreenButton.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							longPressRunnable = new Runnable() {
								@Override
								public void run() {
									copyToClipboard(smaliInput.getText().toString());
								}
							};
							handler.postDelayed(longPressRunnable, 2000); // Adjusted to 2 seconds
							break;
						case MotionEvent.ACTION_UP:
						case MotionEvent.ACTION_CANCEL:
							handler.removeCallbacks(longPressRunnable);
							break;
					}
					return false; // Return false to allow other touch events like clearing the screen
				}
			});

        // Add long press listener to the copyRegexButton to show a toast message
        copyRegexButton.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							longPressRunnable = new Runnable() {
								@Override
								public void run() {
									Toast.makeText(MainActivity.this, "MODD3R", Toast.LENGTH_SHORT).show();
								}
							};
							handler.postDelayed(longPressRunnable, 2000); // Adjusted to 2 seconds
							break;
						case MotionEvent.ACTION_UP:
						case MotionEvent.ACTION_CANCEL:
							handler.removeCallbacks(longPressRunnable);
							break;
					}
					return false; // Return false to allow other touch events
				}
			});
    }

    private void pasteFromClipboard() {
        if (clipboardManager.hasPrimaryClip()) {
            ClipData clipData = clipboardManager.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                CharSequence pasteData = clipData.getItemAt(0).getText();
                if (pasteData != null) {
                    smaliInput.setText(pasteData);
                    smaliInput.setSelection(smaliInput.getText().length()); // Move cursor to the end
                }
            }
        } else {
            Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearScreen() {
        smaliInput.setText("");
    }

    private void copyToClipboard(String text) {
        ClipData clipData = ClipData.newPlainText("regex", text);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private String generateRegex(String input) {
        StringBuilder regexBuilder = new StringBuilder();
        int length = input.length();
        int i = 0;

        while (i < length) {
            char c = input.charAt(i);

            // Handle spaces
            if (c == ' ') {
                int spaceCount = 0;
                while (i < length && input.charAt(i) == ' ') {
                    spaceCount++;
                    i++;
                }
                if (spaceCount > 1) {
                    regexBuilder.append("\\s+");
                } else {
                    regexBuilder.append(" ");
                }
            }
            // Handle newlines
            else if (c == '\n') {
                int newlineCount = 0;
                while (i < length && input.charAt(i) == '\n') {
                    newlineCount++;
                    i++;
                }
                if (newlineCount > 1) {
                    regexBuilder.append("\\n+");
                } else {
                    regexBuilder.append("\\n");
                }
            }
            // Handle p or v followed by digits
            else if ((c == 'p' || c == 'v') && i + 1 < length && Character.isDigit(input.charAt(i + 1))) {
                regexBuilder.append("(").append(c).append("\\d+").append(")");
                while (i + 1 < length && Character.isDigit(input.charAt(i + 1))) {
                    i++;
                }
                i++;
            }
            // Handle /
            else if (c == '/') {
                regexBuilder.append("\\/");
                i++;
            }
            // Handle repeated characters
            else {
                int charCount = 0;
                while (i < length && input.charAt(i) == c) {
                    charCount++;
                    i++;
                }
                if (charCount > 3) {
                    regexBuilder.append("(").append(Pattern.quote(Character.toString(c))).append(")+");
                } else {
                    for (int j = 0; j < charCount; j++) {
                        switch (c) {
                            case '\\':
                                regexBuilder.append("\\\\");
                                break;
                            case '.':
                                regexBuilder.append("\\.");
                                break;
                            case '^':
                                regexBuilder.append("\\^");
                                break;
                            case '$':
                                regexBuilder.append("\\$");
                                break;
                            case '|':
                                regexBuilder.append("\\|");
                                break;
                            case '?':
                                regexBuilder.append("\\?");
                                break;
                            case '*':
                                regexBuilder.append("\\*");
                                break;
                            case '+':
                                regexBuilder.append("\\+");
                                break;
                            case '(':
                                regexBuilder.append("\\(");
                                break;
                            case ')':
                                regexBuilder.append("\\)");
                                break;
                            case '{':
                                regexBuilder.append("\\{");
                                break;
                            case '}':
                                regexBuilder.append("\\}");
                                break;
                            case '[':
                                regexBuilder.append("\\[");
                                break;
                            case ']':
                                regexBuilder.append("\\]");
                                break;
                            case '"':
                                regexBuilder.append("\\\"");
                                break;
                            case '\'':
                                regexBuilder.append("\\'");
                                break;
                            default:
                                regexBuilder.append(c);
                                break;
                        }
                    }
                }
            }
        }

        // Handle repeated words with spaces
        String regex = regexBuilder.toString();
        regex = regex.replaceAll("(\\b\\w+\\b)(\\s+\\1){2,}", "($1)+");

        // Handle repeated substrings within continuous text
        regex = combineRepeatedSubstrings(regex);

        return regex;
    }

    private String combineRepeatedSubstrings(String input) {
        StringBuilder result = new StringBuilder();
        Pattern pattern = Pattern.compile("(\\w{2,}?)(\\1{2,})");
        Matcher matcher = pattern.matcher(input);

        int lastMatchEnd = 0;
        while (matcher.find()) {
            result.append(input, lastMatchEnd, matcher.start());
            result.append("(").append(Pattern.quote(matcher.group(1))).append(")+");
            lastMatchEnd = matcher.end();
        }
        result.append(input.substring(lastMatchEnd));

        return result.toString();
    }
}
