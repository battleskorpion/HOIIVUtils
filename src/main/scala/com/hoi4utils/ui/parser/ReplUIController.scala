package com.hoi4utils.ui.parser
//
//import com.hoi4utils.clausewitz.script.TextAreaPrintStream;

import com.hoi4utils.ui.HOIIVUtilsAbstractController

//import javafx.fxml.FXML;
//import javafx.scene.control.TextArea;
//import javafx.scene.control.TextField;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.LinkedBlockingQueue;
//import com.hoi4utils.clausewitz.script.*;
//import javafx.scene.input.KeyCode;
//import org.apache.poi.ss.formula.functions.T;
//import scala.jdk.javaapi.CollectionConverters;
//

/**
 * TODO CAUTION: This class has java as the commented out code.
 * TODO: Implement or remove this class.
 */
class ReplUIController extends HOIIVUtilsAbstractController {
  //
  //    @FXML
  //    private TextArea replConsoleArea; // This one TextArea will show output and accept input
  //    @FXML
  //    private TextArea replErrorArea;   // for REPL stderr
  //
  //    private final LinkedBlockingQueue<String> lineQueue = new LinkedBlockingQueue<>();
  //    private final String prompt = ">> ";
  //    private int promptPosition = 0;
  //    private final List<PDXScript<?>> pdxScripts = new ArrayList<>();
  //
  //    public ReplUIController() {
  //        setFxmlResource("ReplUI.fxml");
  //        setTitle("HOIIVUtils REPL");
  //    }
  //
  //    /**
  //     * This constructor is used internally by javafx.
  //     * Use {@link #ReplUIController()} to create a new instance.
  //     * Then call {@link #open(Object...)} to set the properties.
  //     *
  //     * @param pdxScripts
  //     */
  //    @SuppressWarnings("unused")
  //    public ReplUIController(List<PDXScript<T>> pdxScripts) {
  //        this.pdxScripts.addAll(pdxScripts);
  //    }
  //
  //    @FXML
  //    private void initialize() {
  //        // Set the initial prompt in the TextArea.
  //        replConsoleArea.setText(prompt);
  //        promptPosition = replConsoleArea.getText().length();
  //
  //        // Intercept key events on the TextArea.
  //        replConsoleArea.setOnKeyPressed(e -> {
  //            // When Enter is pressed...
  //            if (e.getCode() == KeyCode.ENTER) {
  //                // Get the text from the prompt onward.
  //                String text = replConsoleArea.getText();
  //                String command = text.substring(promptPosition);
  //                // Enqueue the command for the REPL.
  //                lineQueue.offer(command);
  //                // Optionally, you might want to echo the command again or let the REPL print the result.
  //                // Append a new prompt.
  //                replConsoleArea.appendText(prompt);
  //                promptPosition = replConsoleArea.getText().length();
  //                e.consume();
  //            }
  //            // Prevent the caret from moving into the “read-only” region (before the prompt)
  //            if (replConsoleArea.getCaretPosition() < promptPosition) {
  //                replConsoleArea.positionCaret(promptPosition);
  //            }
  //        });
  //
  //        // Create one PrintStream that writes to our combined console.
  //        TextAreaPrintStream consoleStream = new TextAreaPrintStream(replConsoleArea);
  //        replConsoleArea.setEditable(true);
  //
  //        // Start the embedded REPL, using the same stream for both output and error.
  //        ScalaEmbeddedRepl$.MODULE$.openRepl(
  //                consoleStream,
  //                new TextAreaPrintStream(replErrorArea),
  //                lineQueue,
  //                CollectionConverters.asScala(pdxScripts)
  //        );
  //    }
}