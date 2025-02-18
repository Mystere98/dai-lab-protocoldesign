package ch.heig.dai.lab.protocoldesign;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;

import static java.nio.charset.StandardCharsets.UTF_8;

public class RunnableClientHandler implements Runnable {
    final Socket socket;
    final Worker worker;

    public RunnableClientHandler(Socket socket, Worker worker) {
        this.socket = socket;
        this.worker = worker;
    }

    public void run() {
        try (var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
             var out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8))) {
            System.out.println("Client connected");

            // sending operations
            System.out.println("Sending operations");
            Operation operation = new Operation();
            out.write(new Gson().toJson(operation));
            out.newLine();
            out.flush();

            String line;
            while (!(line = in.readLine()).equals("exit")) {
                try {
                    System.out.println("Received request: " + line);
                    try {
                        out.write(new Gson().toJson(worker.work(new Gson().fromJson(line, Request.class))));
                        out.newLine();
                        out.flush();
                    } catch (Exception e) {
                        out.write(new Gson().toJson(new Result("0", 0, false, "Invalid JSON")));
                        out.newLine();
                        out.flush();
                        continue;
                    }

                } catch (Exception e) {
                    out.write(new Gson().toJson(new Result("asd", 0, false, "Invalid operator")));
                }
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error in ClientHandler: " + e.getMessage());
        }
    }
}
