import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import java.nio.file.*;

public class ContadorDeLinksConVirtualThreads {

    private static String obtenerDominio(String url) throws MalformedURLException {
        return new URL(url).getHost();
    }

    private static int contarLinksInternos(String url) {
        try {
            String contenido = new String(new URL(url).openStream().readAllBytes());
            String dominio = obtenerDominio(url);

            Pattern pattern = Pattern.compile("href\\s*=\\s*\"(https?://[^\"]+)\"");
            Matcher matcher = pattern.matcher(contenido);

            int contador = 0;
            while (matcher.find()) {
                String link = matcher.group(1);
                if (link.contains(dominio)) {
                    contador++;
                }
            }

            return contador;
        } catch (Exception e) {
            System.err.println("Error procesando " + url + ": " + e.getMessage());
            return -1;
        }
    }

    public static void main(String[] args) throws IOException {
        List<String> urls = Files.readAllLines(Path.of("C:/Users/Usuario/OneDrive/Escritorio/URLsConsulta.txt"));
        Map<String, Integer> resultados = new ConcurrentHashMap<>();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> tareas = new ArrayList<>();

            for (String url : urls) {
                Future<?> tarea = executor.submit(() -> {
                    int cantidad = contarLinksInternos(url);
                    resultados.put(url, cantidad);
                });
                tareas.add(tarea);
            }

            for (Future<?> tarea : tareas) {
                try {
                    tarea.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        try (PrintWriter out = new PrintWriter("resultados.txt")) {
            for (Map.Entry<String, Integer> entrada : resultados.entrySet()) {
                out.println(entrada.getKey() + " => " + entrada.getValue() + " links internos");
            }
        }

        System.out.println("Proceso completado. Ver archivo resultados.txt.");
    }
}
