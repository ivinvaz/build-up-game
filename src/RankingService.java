package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

/**
 * Persistência local do placar em CSV. Campos textuais usam Base64 URL-safe
 * para preservar vírgulas, quebras de linha e acentos sem bibliotecas externas.
 */
public final class RankingService {
    private static final Path ARQUIVO = Paths.get("data", "ranking.csv");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private RankingService() { }

    public static class Campanha {
        public final String jogador;
        public final int pontos;
        public final boolean venceu;
        public final int inimigos;
        public final int vida;
        public final String itens;
        public final String data;

        Campanha(String jogador, int pontos, boolean venceu, int inimigos, int vida, String itens, String data) {
            this.jogador = jogador; this.pontos = pontos; this.venceu = venceu;
            this.inimigos = inimigos; this.vida = vida; this.itens = itens; this.data = data;
        }
    }

    public static synchronized void registrar(String jogador, int pontos, boolean venceu, int inimigos, int vida, String itens) {
        List<Campanha> campanhas = listar();
        campanhas.add(new Campanha(jogador, pontos, venceu, inimigos, vida, itens, LocalDateTime.now().format(DATA)));
        campanhas.sort(Comparator.comparingInt((Campanha c) -> c.pontos).reversed());
        if (campanhas.size() > 10) campanhas = new ArrayList<>(campanhas.subList(0, 10));
        gravar(campanhas);
    }

    public static synchronized List<Campanha> listar() {
        List<Campanha> campanhas = new ArrayList<>();
        if (!Files.exists(ARQUIVO)) return campanhas;
        try (BufferedReader reader = Files.newBufferedReader(ARQUIVO, StandardCharsets.UTF_8)) {
            String linha;
            boolean cabecalho = true;
            while ((linha = reader.readLine()) != null) {
                if (cabecalho) { cabecalho = false; continue; }
                String[] p = linha.split(",", -1);
                if (p.length != 7) continue;
                try {
                    campanhas.add(new Campanha(decodificar(p[0]), Integer.parseInt(p[1]), Boolean.parseBoolean(p[2]),
                            Integer.parseInt(p[3]), Integer.parseInt(p[4]), decodificar(p[5]), decodificar(p[6])));
                } catch (RuntimeException ignorado) { /* ignora somente registros corrompidos */ }
            }
        } catch (IOException e) {
            System.err.println("Não foi possível ler o ranking: " + e.getMessage());
        }
        campanhas.sort(Comparator.comparingInt((Campanha c) -> c.pontos).reversed());
        return campanhas;
    }

    private static void gravar(List<Campanha> campanhas) {
        try {
            Files.createDirectories(ARQUIVO.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(ARQUIVO, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                writer.write("jogador,pontos,venceu,inimigos,vida,itens,data");
                writer.newLine();
                for (Campanha c : campanhas) {
                    writer.write(codificar(c.jogador) + "," + c.pontos + "," + c.venceu + "," + c.inimigos + ","
                            + c.vida + "," + codificar(c.itens) + "," + codificar(c.data));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Não foi possível salvar o ranking: " + e.getMessage());
        }
    }

    private static String codificar(String valor) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(valor.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodificar(String valor) {
        return new String(Base64.getUrlDecoder().decode(valor), StandardCharsets.UTF_8);
    }
}
