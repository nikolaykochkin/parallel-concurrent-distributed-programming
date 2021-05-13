package edu.coursera.distributed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A basic and very limited implementation of a file server that responds to GET
 * requests from HTTP clients.
 */
public final class FileServer {
    /**
     * Main entrypoint for the basic file server.
     *
     * @param socket Provided socket to accept connections on.
     * @param fs     A proxy filesystem to serve files from. See the PCDPFilesystem
     *               class for more detailed documentation of its usage.
     * @param ncores The number of cores that are available to your
     *               multi-threaded file server. Using this argument is entirely
     *               optional. You are free to use this information to change
     *               how you create your threads, or ignore it.
     * @throws IOException If an I/O error is detected on the server. This
     *                     should be a fatal error, your file server
     *                     implementation is not expected to ever throw
     *                     IOExceptions during normal operation.
     */
    public void run(final ServerSocket socket, final PCDPFilesystem fs,
                    final int ncores) throws IOException {
        /*
         * Enter a spin loop for handling client requests to the provided
         * ServerSocket object.
         */
        ExecutorService executorService = Executors.newWorkStealingPool();

        while (true) {
            Socket clientSocket = socket.accept();
            executorService.submit(() -> {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream())) {
                    String line = in.readLine();
                    assert line != null;
                    assert line.startsWith("GET");
                    String path = line.split("\\s+")[1];
                    String content = fs.readFile(new PCDPPath(path));
                    if (content != null) {
                        out.write("HTTP/1.0 200 OK\r\n");
                        out.write("Server: FileServer\r\n");
                        out.write("\r\n");
                        out.write(content);
                        out.write("\r\n");
                    } else {
                        out.write("HTTP/1.0 404 Not Found\r\n");
                        out.write("Server: FileServer\r\n");
                        out.write("\r\n");
                    }
                    out.flush();
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}

