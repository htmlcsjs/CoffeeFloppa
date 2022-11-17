package xyz.htmlcsjs.coffeeFloppa.asm;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FloppaClassLoader extends ClassLoader {
    private final List<String> packagesToExclude;
    private final ClassLoader parent = getClass().getClassLoader();
    public FloppaClassLoader(ClassLoader parent, List<String> packagesToExclude) {
        super(parent);
        this.packagesToExclude = packagesToExclude;
    }

    @Override
    public String toString() {
        return FloppaClassLoader.class.getName();
    }


    protected Class<?> getClass(String name) throws ClassNotFoundException {
        String file = name.replace('.', File.separatorChar) + ".class";
        byte[] b;
        try {
            // This loads the byte code data from the file
            b = loadClassData(file);
            // defineClass is inherited from the ClassLoader class
            // and converts the byte array into a Class
            b = FloppaTransformer.transform(this, name, b);
            Class<?> c = defineClass(name, b, 0, b.length);
            resolveClass(c);
            return c;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        for (final String pkg : packagesToExclude) {
            if (name.startsWith(pkg)) {
                return parent.loadClass(name);
            }
        }
        return getClass(name);
    }

    /**
     * Load the class file into byte array
     *
     * @param name The name of the class e.g. com.codeslices.test.TestClass}
     * @return The class file as byte array
     */
    private byte[] loadClassData(String name) throws IOException, ClassNotFoundException {
        // Opening the file
        InputStream stream = parent.getResourceAsStream(name);
        int size = 0;
        if (stream != null) {
            size = stream.available();
        } else {
            throw new ClassNotFoundException();
        }
        byte[] buff = new byte[size];
        DataInputStream in = new DataInputStream(stream);
        // Reading the binary data
        in.readFully(buff);
        in.close();
        return buff;
    }
}

