package miniJava;

import java.util.ArrayList;
import java.util.List;

public class ErrorReporter {
    List<String> errorQueue;

    public ErrorReporter() {
        errorQueue = new ArrayList<>();
    }

    public boolean isEmpty() {
        return errorQueue.isEmpty();
    }

    public void showErrorQueue() {
        System.out.println(errorQueue.get(0));
    }

    public void reportError(String s) {
        errorQueue.add(s);
    }
}

