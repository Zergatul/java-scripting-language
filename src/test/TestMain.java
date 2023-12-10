import com.zergatul.scripting.compiler.ScriptCompileException;
import com.zergatul.scripting.compiler.ScriptingLanguageCompiler;
import com.zergatul.scripting.generated.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TestMain {

    public static void main(String[] args) {
        testFile("booleans.txt");
        testFile("test-script.txt");
        testFile("variable-script.txt");
        testFile("casts.txt");
        testFile("parameters.txt");
        testFile("arrays.txt");
        testFile("for-loop.txt");
        testFile("foreach-loop.txt");
        testFile("strings.txt");
        testFile("lambda.txt");
        testFile("static-variables.txt");
    }

    private static void testFile(String name) {
        System.out.println(name);
        TestRoot.Assert.clear();

        var compiler = new ScriptingLanguageCompiler(TestRoot.class);
        try {
            File file = new File(name);
            if (!file.exists()) {
                System.out.println("File " + name + " doesn't exist");
                return;
            }
            InputStream stream = new FileInputStream(file);
            String code = readStreamText(stream);

            Runnable program = compiler.compile(code);
            program.run();
        } catch (ParseException | ScriptCompileException | IOException e) {
            e.printStackTrace();
            return;
        } catch (Throwable e) {
            System.out.println("Generic exception!");
            e.printStackTrace();
        }

        int successCount = TestRoot.Assert.getSuccess().size();
        int failCount = TestRoot.Assert.getFail().size();
        System.out.println("Success: " + successCount +", Fail: " + failCount);
        if (failCount > 0) {
            System.out.println("Fails:");
            for (var x : TestRoot.Assert.getFail()) {
                System.out.println(x);
            }
        }

        System.out.println("");
    }

    private static String readStreamText(InputStream stream) throws IOException {
        int bufferSize = 1024;
        char[] buffer = new char[bufferSize];
        StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(stream, StandardCharsets.UTF_8);
        for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
            out.append(buffer, 0, numRead);
        }
        return out.toString();
    }

    public static class TestRoot {
        public static Assertion Assert = new Assertion();
        public static Deep deep = new Deep();
        public static Methods methods = new Methods();
        public static Events events = new Events();
    }

    public static class Assertion {

        private List<String> success = new ArrayList<>();
        private List<String> fail = new ArrayList<>();

        public List<String> getSuccess() {
            return success;
        }

        public List<String> getFail() {
            return fail;
        }

        public void isTrue(String name, boolean value) {
            (value ? success : fail).add(name);
        }

        public void isFalse(String name, boolean value) {
            (!value ? success : fail).add(name);
        }

        public void equals(String name, double value1, double value2) {
            (value1 == value2 ? success : fail).add(name);
        }

        public void notEquals(String name, double value1, double value2) {
            (value1 != value2 ? success : fail).add(name);
        }

        public void clear() {
            success.clear();
            fail.clear();
        }
    }

    public static class Deep {

        public Deep1 deep = new Deep1();

        public int getValue() {
            return 987;
        }
    }

    public static class Deep1 {

        public Deep2 deep = new Deep2();

        public int getValue() {
            return 101;
        }
    }

    public static class Deep2 {

        public Deep3 deep = new Deep3();

        public int getValue() {
            return 654;
        }
    }

    public static class Deep3 {

        public Deep4 deep = new Deep4();

        public int getValue() {
            return 321;
        }
    }

    public static class Deep4 {

        public int getValue() {
            return 100;
        }
    }

    public static class Methods {
        public double m1(int x, int y, int z, String s) {
            return x;
        }

        public double m1(double x, double y, double z, String s) {
            return y;
        }

        public String toString(int value) {
            return "int";
        }

        public String toString(double value) {
            return "double";
        }
    }

    public static class Events {

        private boolean checked = false;

        public void onTick(Runnable action) {
            action.run();
        }

        public void markUnchecked() {
            checked = false;
        }

        public void check() {
            checked = true;
        }

        public boolean isChecked() {
            return checked;
        }
    }

    public static class Debug {
        public void print(int value) {
            System.out.println(value);
        }
    }
}