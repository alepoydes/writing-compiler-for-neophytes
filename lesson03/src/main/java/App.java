package wcn.lexer;

import java.io.*;
import java.nio.charset.StandardCharsets;

import wcn.lexer.FSA;

public class App {
    
    public static void main(String[] args) throws IOException {
        Reader input_stream=new InputStreamReader(System.in, StandardCharsets.UTF_8);
        Writer output_stream=new OutputStreamWriter(System.out/*, StandardCharsets.UTF_8*/);
        while(true) {
            output_stream.write(input_stream.read());
            output_stream.flush();
        }
    }
}
