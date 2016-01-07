package com.github.lawena.util;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Collection of helpful JavaFX utility methods.
 */
public class FXUtils {

    private static final ExecutorService pool = Executors.newCachedThreadPool();

    /**
     * Performs an operation which computes a value in a background thread, then consume that value in the FX Application Thread.
     *
     * @param supplyInBackground a supplier processed in the background
     * @param consumeInFxThread  a consumer processed in the FX Application Thread
     * @param <T>                the type of the computed value
     */
    public static <T> void asyncToFxThread(Supplier<T> supplyInBackground, Consumer<T> consumeInFxThread) {
        CompletableFuture.supplyAsync(supplyInBackground, pool).thenAcceptAsync(consumeInFxThread, Platform::runLater);
    }

    public static List<Runnable> shutdownPool() {
        return pool.shutdownNow();
    }

    /**
     * Restricts the possible user input to the given node.
     *
     * @param node         the node to configure
     * @param allowedChars a string representing the domain of KeyEvent.KEY_TYPED events that will be allowed as user input to the node
     */
    public static void restrictInput(Node node, String allowedChars) {
        node.addEventFilter(KeyEvent.KEY_TYPED, ke -> {
            if (!allowedChars.contains(ke.getCharacter())) {
                ke.consume();
            }
        });
    }

    /**
     * Construct a boolean binding given by a source and a regex Pattern and bind it to a BooleanProperty.
     *
     * @param pattern compiled regex that source will be matched against
     * @param target  binding recipient property
     * @param source  property used to create the binding
     */
    public static void patternBinding(Pattern pattern, BooleanProperty target, StringProperty source) {
        BooleanBinding binding =
                Bindings.createBooleanBinding(() -> pattern.matcher(source.get()).matches(), source);
        target.bind(binding);
    }

    /**
     * Binds a TableColumn's preferred and maximum width to a certain proportion and a margin to further tweak the resulting width.
     * The column will not be resized by the user afterwards.
     *
     * @param column
     * @param table
     * @param prop
     * @param margin
     */
    public static void configureColumn(TableColumn<?, ?> column, TableView<?> table, double prop, int margin) {
        column.prefWidthProperty().bind(table.widthProperty().multiply(prop).subtract(margin));
        column.maxWidthProperty().bind(column.prefWidthProperty());
        column.setResizable(false);
    }

    /**
     * Display an informational Alert with the given parameters.
     *
     * @param title
     * @param header
     * @param content
     * @return the action taken by the user on the displayed dialog.
     */
    public static Optional<ButtonType> showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait();
    }

    public static Optional<ButtonType> showWarning(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait();
    }

    public static Optional<ButtonType> showWarning(String title, String header, String content, Node expanded) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.getDialogPane().setExpandableContent(expanded);
        return alert.showAndWait();
    }

    public static Optional<ButtonType> showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait();
    }

    /**
     * Runs the specified {@link Runnable} on the
     * JavaFX application thread and waits for completion.
     *
     * @param action the {@link Runnable} to run
     * @throws NullPointerException if {@code action} is {@code null}
     */
    public static void runAndWait(Runnable action) {
        if (action == null)
            throw new NullPointerException("action");
        // run synchronously on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }
        // queue on JavaFX thread and wait for completion
        final CountDownLatch doneLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                doneLatch.countDown();
            }
        });
        try {
            doneLatch.await();
        } catch (InterruptedException e) {
            // ignore exception
        }
    }


    /**
     * Makes sure that the given action is run on the JavaFX application thread,
     * executing it synchronously if the call is already made on the JavaFX application thread.
     *
     * @param action the action to perform
     */
    public static void ensureRunLater(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    /**
     * Makes sure that the given action is run on the JavaFX application thread, executing it synchronously if the call
     * is already made on the JavaFX application thread, wait for its completion and retrieve either the expected result
     * or a given exceptional value.
     *
     * @param action        the action to perform
     * @param exceptionally the value retrieve if the result could not be retrieved
     * @param <T>           the type of the supplier
     * @return either the result of the given action (if it could be completed) or the exceptional value
     * (in case an exception was thrown while waiting for the result)
     */
    public static <T> T ensureRunAndGet(Supplier<T> action, T exceptionally) {
        if (Platform.isFxApplicationThread()) {
            return action.get();
        }
        try {
            return CompletableFuture.supplyAsync(action, Platform::runLater).get();
        } catch (InterruptedException | ExecutionException e) {
            return exceptionally;
        }
    }

    private FXUtils() {

    }
}
