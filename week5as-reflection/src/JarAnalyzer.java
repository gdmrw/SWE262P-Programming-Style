import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarAnalyzer {

    public static void main(String[] args) {
        String filepath = args[0];
        try {
            JarFile jarFile = new JarFile(filepath);
            Enumeration<JarEntry> e = jarFile.entries();

            while (e.hasMoreElements()) {
                JarEntry entry = e.nextElement();
                if (!entry.getName().contains("META-INF") && entry.getName().endsWith(".class")) {
                    String classFullName = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);

                    try {
                        Class<?> c = Class.forName(classFullName);

                        int publicMethods = 0, privateMethods = 0, protectedMethods = 0, staticMethods = 0, fieldsCount = 0;
                        Method[] methods = c.getDeclaredMethods();
                        for (Method method : methods) {
                            if (Modifier.isPublic(method.getModifiers())) publicMethods++;
                            if (Modifier.isPrivate(method.getModifiers())) privateMethods++;
                            if (Modifier.isProtected(method.getModifiers())) protectedMethods++;
                            if (Modifier.isStatic(method.getModifiers())) staticMethods++;
                        }

                        Field[] fields = c.getDeclaredFields();
                        fieldsCount = fields.length;

                        System.out.println("----------" + classFullName + "----------");
                        System.out.println("  Public methods: " + publicMethods);
                        System.out.println("  Private methods: " + privateMethods);
                        System.out.println("  Protected methods: " + protectedMethods);
                        System.out.println("  Static methods: " + staticMethods);
                        System.out.println("  Fields: " + fieldsCount);

                    } catch (ClassNotFoundException classNotFoundException) {
                        classNotFoundException.printStackTrace();
                    }
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
