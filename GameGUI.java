import src.Arma;
import src.Artefato;
import src.Inimigo;
import src.Mochila;
import src.Pocao;
import src.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface Swing do Build Up Game. A interface chama diretamente as classes
 * de domínio já utilizadas pela versão CLI.
 */
public class GameGUI extends JFrame {
    private static final Color BACKGROUND = new Color(28, 32, 42);
    private static final Color PANEL = new Color(40, 46, 59);
    private static final Color ACCENT = new Color(82, 150, 230);
    private static final Color TEXT = new Color(235, 239, 245);

    private Usuario usuario;
    private Mochila mochila;
    private int etapa = 1;
    private int inimigosDerrotados = 0;
    private Inimigo inimigoAtual;

    private final JLabel status = new JLabel();
    private final DefaultListModel<String> inventoryModel = new DefaultListModel<>();
    private final JList<String> inventoryList = new JList<>(inventoryModel);
    private final JTextArea log = new JTextArea();
    private final JTextField itemName = new JTextField(16);
    private final JTextField itemValue = new JTextField(6);
    private final JComboBox<String> itemType = new JComboBox<>(new String[]{"Arma", "Artefato", "Poção"});
    private final JComboBox<String> category = new JComboBox<>(new String[]{"Física", "Mágica"});
    private final JCheckBox confirmBattle = new JCheckBox("Confirmar antes de cada batalha", true);
    private final JCheckBox verboseLog = new JCheckBox("Exibir detalhes dos cálculos", false);
    private final JTable rankingTable = new JTable();

    public GameGUI() {
        super("Build Up Game");
        String nome = JOptionPane.showInputDialog(this, "Qual é o seu nome?", "Nova campanha", JOptionPane.QUESTION_MESSAGE);
        if (nome == null || nome.trim().isEmpty()) nome = "Aventureiro";
        iniciarCampanha(nome.trim());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));
        setSize(1080, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout(12, 12));

        JLabel title = new JLabel("BUILD UP GAME  |  Sobreviva a três batalhas");
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(14, 18, 4, 18));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Campanha", buildGamePanel());
        tabs.addTab("Ranking", buildRankingPanel());
        tabs.setBackground(PANEL);
        tabs.setForeground(TEXT);
        add(tabs, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BACKGROUND);
        footer.setBorder(new EmptyBorder(0, 18, 12, 18));
        status.setForeground(new Color(180, 205, 235));
        footer.add(status, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        atualizarTela();
        registrar("Prepare sua mochila. Você precisa de pelo menos uma arma para iniciar.");
    }

    private void iniciarCampanha(String nome) {
        usuario = new Usuario(nome);
        mochila = usuario.getUsuarioMochila();
        etapa = 1;
        inimigosDerrotados = 0;
        inimigoAtual = null;
    }

    private JPanel buildGamePanel() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(BACKGROUND);
        root.setBorder(new EmptyBorder(8, 12, 12, 12));

        JPanel left = painel("Mochila e gerenciamento");
        left.setLayout(new BorderLayout(8, 8));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PANEL);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 5, 4, 5);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.gridy = 0; form.add(label("Tipo"), c);
        c.gridx = 1; form.add(itemType, c);
        c.gridx = 0; c.gridy++; form.add(label("Categoria da arma"), c);
        c.gridx = 1; form.add(category, c);
        c.gridx = 0; c.gridy++; form.add(label("Nome (opcional)"), c);
        c.gridx = 1; form.add(itemName, c);
        c.gridx = 0; c.gridy++; form.add(label("Valor: dano / bônus / cura"), c);
        c.gridx = 1; form.add(itemValue, c);
        c.gridx = 0; c.gridy++; c.gridwidth = 2;
        form.add(button("Adicionar item", this::adicionarItem), c);
        c.gridy++;
        form.add(button("Atualizar item selecionado", this::atualizarItem), c);
        c.gridy++;
        form.add(button("Remover item selecionado", this::removerItem), c);
        itemType.addActionListener(e -> atualizarCamposTipo());

        inventoryList.setBackground(new Color(32, 37, 48));
        inventoryList.setForeground(TEXT);
        inventoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane inventoryScroll = new JScrollPane(inventoryList);
        inventoryScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(80, 92, 112)), "Itens armazenados"));
        inventoryScroll.getViewport().setBackground(new Color(32, 37, 48));
        left.add(form, BorderLayout.NORTH);
        left.add(inventoryScroll, BorderLayout.CENTER);

        JPanel right = painel("Aventura");
        right.setLayout(new BorderLayout(8, 8));
        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setBackground(new Color(24, 28, 37));
        log.setForeground(TEXT);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        right.add(new JScrollPane(log), BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(0, 2, 7, 7));
        actions.setBackground(PANEL);
        actions.add(button("Iniciar / próxima batalha", this::batalhar));
        actions.add(button("Usar poção", this::usarPocao));
        actions.add(button("Nova campanha", this::novaCampanha));
        actions.add(button("Atualizar mochila", this::atualizarTela));
        actions.add(confirmBattle);
        actions.add(verboseLog);
        confirmBattle.setBackground(PANEL);
        confirmBattle.setForeground(TEXT);
        verboseLog.setBackground(PANEL);
        verboseLog.setForeground(TEXT);
        right.add(actions, BorderLayout.SOUTH);

        left.setPreferredSize(new Dimension(390, 500));
        root.add(left, BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildRankingPanel() {
        JPanel panel = painel("Melhores campanhas");
        panel.setLayout(new BorderLayout(8, 8));
        JTextArea ranking = new JTextArea();
        ranking.setEditable(false);
        ranking.setBackground(new Color(24, 28, 37));
        ranking.setForeground(TEXT);
        ranking.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        panel.add(new JScrollPane(ranking), BorderLayout.CENTER);
        panel.add(button("Recarregar ranking", () -> carregarRanking(ranking)), BorderLayout.SOUTH);
        carregarRanking(ranking);
        return panel;
    }

    private JPanel painel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(80, 92, 112)), title,
                0, 0, new Font("SansSerif", Font.BOLD, 14), TEXT));
        return panel;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        return label;
    }

    private JButton button(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        button.addActionListener(e -> action.run());
        return button;
    }

    private void atualizarCamposTipo() {
        boolean arma = "Arma".equals(itemType.getSelectedItem());
        category.setEnabled(arma);
        itemValue.setToolTipText(arma ? "Dano da arma" : ("Artefato".equals(itemType.getSelectedItem()) ? "Aumento de dano" : "Cura em porcentagem"));
    }

    private void adicionarItem() {
        String tipo = (String) itemType.getSelectedItem();
        String nome = itemName.getText().trim();
        try {
            if ("Arma".equals(tipo)) {
                Arma arma = usuario.criarArma("Física".equals(category.getSelectedItem()) ? "fisico" : "magico");
                if (!nome.isEmpty()) arma.editarNome(nome);
                if (!itemValue.getText().trim().isEmpty()) arma.editarDano(Integer.parseInt(itemValue.getText().trim()));
                if (mochila.consultarEspacoUsado() + arma.getPeso() > 5) {
                    aviso("Não há espaço suficiente na mochila."); return;
                }
                mochila.addItem(arma, null, null);
                registrar("Arma adicionada: " + arma.getNome() + " (" + arma.getDano(mochila) + " de dano).");
            } else if ("Artefato".equals(tipo)) {
                Artefato artefato = new Artefato();
                if (!nome.isEmpty()) artefato.editarNome(nome);
                if (!itemValue.getText().trim().isEmpty()) artefato.editarAumento(Integer.parseInt(itemValue.getText().trim()));
                mochila.addItem(null, artefato, null);
                registrar("Artefato adicionado: " + artefato.getNome() + " (+" + artefato.getAumento() + ").");
            } else {
                Pocao pocao = new Pocao();
                if (!itemValue.getText().trim().isEmpty()) pocao.editarCura(Integer.parseInt(itemValue.getText().trim()));
                if (mochila.consultarEspacoUsado() + pocao.getPeso() > 5) {
                    aviso("Não há espaço suficiente na mochila."); return;
                }
                mochila.addItem(null, null, pocao);
                registrar("Poção adicionada (" + pocao.getCura() + "% de cura).");
            }
            limparFormulario();
            atualizarTela();
        } catch (NumberFormatException ex) {
            aviso("O valor informado precisa ser um número inteiro.");
        } catch (IllegalArgumentException ex) {
            aviso(ex.getMessage());
        }
    }

    private void atualizarItem() {
        int index = inventoryList.getSelectedIndex();
        if (index < 0) { aviso("Selecione um item na mochila."); return; }
        ItemSelecionado selecionado = itens().get(index);
        String nome = itemName.getText().trim();
        try {
            if (selecionado.arma != null) {
                if (!nome.isEmpty()) selecionado.arma.editarNome(nome);
                if (!itemValue.getText().trim().isEmpty()) selecionado.arma.editarDano(Integer.parseInt(itemValue.getText().trim()));
                registrar("Arma atualizada: " + selecionado.arma.getNome() + ".");
            } else if (selecionado.artefato != null) {
                if (!nome.isEmpty()) selecionado.artefato.editarNome(nome);
                if (!itemValue.getText().trim().isEmpty()) selecionado.artefato.editarAumento(Integer.parseInt(itemValue.getText().trim()));
                registrar("Artefato atualizado: " + selecionado.artefato.getNome() + ".");
            } else {
                if (!itemValue.getText().trim().isEmpty()) selecionado.pocao.editarCura(Integer.parseInt(itemValue.getText().trim()));
                registrar("Poção atualizada.");
            }
            atualizarTela();
        } catch (NumberFormatException ex) {
            aviso("O valor informado precisa ser um número inteiro.");
        } catch (IllegalArgumentException ex) {
            aviso(ex.getMessage());
        }
    }

    private void removerItem() {
        int index = inventoryList.getSelectedIndex();
        if (index < 0) { aviso("Selecione um item na mochila."); return; }
        ItemSelecionado selecionado = itens().get(index);
        if (selecionado.arma != null) mochila.removerItem(selecionado.arma, null, null);
        else if (selecionado.artefato != null) mochila.removerItem(null, selecionado.artefato, null);
        else mochila.removerItem(null, null, selecionado.pocao);
        registrar("Item removido da mochila.");
        atualizarTela();
    }

    private static class ItemSelecionado {
        Arma arma; Artefato artefato; Pocao pocao;
        ItemSelecionado(Arma a, Artefato b, Pocao p) { arma=a; artefato=b; pocao=p; }
    }

    private List<ItemSelecionado> itens() {
        List<ItemSelecionado> result = new ArrayList<>();
        for (Arma a : mochila.consultarArmas()) result.add(new ItemSelecionado(a, null, null));
        for (Artefato a : mochila.consultarArtefatos()) result.add(new ItemSelecionado(null, a, null));
        for (Pocao p : mochila.consultarPocoes()) result.add(new ItemSelecionado(null, null, p));
        return result;
    }

    private void batalhar() {
        if (mochila.consultarArmas().isEmpty()) {
            aviso("Adicione ao menos uma arma antes de iniciar as batalhas."); return;
        }
        if (inimigosDerrotados >= 3) { aviso("Você já concluiu esta campanha. Inicie uma nova."); return; }
        inimigoAtual = new Inimigo(etapa);
        String resumo = "Etapa " + etapa + "/3\nInimigo: " + inimigoAtual.getNome()
                + "\nCategoria: " + inimigoAtual.getCategoria()
                + "\nVida: " + inimigoAtual.getVida() + " | Dano: " + inimigoAtual.getDano()
                + "\nSua vida: " + usuario.getVida();
        if (confirmBattle.isSelected() && JOptionPane.showConfirmDialog(this, resumo + "\n\nEnfrentar?", "Batalha", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            registrar("Você recuou antes de enfrentar " + inimigoAtual.getNome() + "."); return;
        }
        int dano = mochila.getDanoArmas(inimigoAtual.getCategoria());
        java.util.HashMap<String, Object> resultado = usuario.batalhar(inimigoAtual);
        boolean venceu = (Boolean) resultado.get("venceu");
        registrar("ETAPA " + etapa + " — " + inimigoAtual.getNome() + " (" + inimigoAtual.getCategoria() + ")");
        if (verboseLog.isSelected()) registrar("Dano calculado da build: " + dano + " | Dano inimigo: " + inimigoAtual.getDano());
        registrar((String) resultado.get("mensagem"));
        if (venceu) {
            inimigosDerrotados++;
            Artefato premio = new Artefato();
            mochila.addItem(null, premio, null);
            registrar("Recompensa: " + premio.getNome() + " (+" + premio.getAumento() + " de dano).");
            if (inimigosDerrotados == 3) {
                registrar("VITÓRIA! Você derrotou os três inimigos.");
                salvarCampanha(true);
                JOptionPane.showMessageDialog(this, "Campanha concluída! Pontuação: " + pontuacao());
            } else {
                etapa++;
                registrar("Prepare sua build para a próxima etapa.");
            }
        } else {
            registrar("FIM DE JOGO. Sua campanha foi registrada no ranking.");
            salvarCampanha(false);
            JOptionPane.showMessageDialog(this, "Fim de jogo! Pontuação: " + pontuacao());
        }
        atualizarTela();
    }

    private void usarPocao() {
        if (mochila.consultarPocoes().isEmpty()) { aviso("Você não possui poções."); return; }
        if (usuario.getVida() >= usuario.getVidaMaxima()) { aviso("Sua vida já está cheia."); return; }
        Pocao pocao = mochila.consultarPocoes().get(0);
        int vidaAntes = usuario.getVida();
        usuario.curar(pocao.getCura());
        mochila.removerItem(null, null, pocao);
        registrar("Poção utilizada: vida " + vidaAntes + " → " + usuario.getVida() + ".");
        atualizarTela();
    }

    private int pontuacao() {
        return inimigosDerrotados * 1000 + usuario.getVida() * 10 + usuario.getMoedas() * 25;
    }

    private String resumoItens() {
        StringBuilder b = new StringBuilder();
        for (Arma a : mochila.consultarArmas()) b.append("Arma: ").append(a.getNome()).append(" (").append(a.getCategoria()).append(", dano ").append(a.getDano(mochila)).append("); ");
        for (Artefato a : mochila.consultarArtefatos()) b.append("Artefato: ").append(a.getNome()).append(" (+").append(a.getAumento()).append("); ");
        for (Pocao p : mochila.consultarPocoes()) b.append("Poção (cura ").append(p.getCura()).append("%); ");
        return b.toString();
    }

    private void salvarCampanha(boolean venceu) {
        RankingService.registrar(usuario.getNome(), pontuacao(), venceu, inimigosDerrotados, usuario.getVida(), resumoItens());
    }

    private void carregarRanking(JTextArea area) {
        StringBuilder b = new StringBuilder("POS  PONTOS  RESULTADO  JOGADOR  INIMIGOS  VIDA\n");
        b.append("---------------------------------------------------------------\n");
        List<RankingService.Campanha> campanhas = RankingService.listar();
        if (campanhas.isEmpty()) b.append("Nenhuma campanha registrada ainda.\n");
        int pos = 1;
        for (RankingService.Campanha c : campanhas) {
            b.append(String.format("%-4d %-7d %-9s %-18s %-8d %d%n", pos++, c.pontos, c.venceu ? "VITÓRIA" : "DERROTA", c.jogador, c.inimigos, c.vida));
            b.append("     Itens: ").append(c.itens).append("\n");
            b.append("     Data: ").append(c.data).append("\n\n");
        }
        area.setText(b.toString());
    }

    private void novaCampanha() {
        String nome = JOptionPane.showInputDialog(this, "Nome do jogador:", usuario.getNome());
        if (nome == null || nome.trim().isEmpty()) return;
        iniciarCampanha(nome.trim());
        log.setText("");
        registrar("Nova campanha iniciada. Prepare sua mochila!");
        atualizarTela();
    }

    private void atualizarTela() {
        status.setText("Jogador: " + usuario.getNome() + "   |   Vida: " + usuario.getVida() + "/" + usuario.getVidaMaxima()
                + "   |   Mochila: " + mochila.consultarEspacoUsado() + "/5   |   Moedas: " + usuario.getMoedas()
                + "   |   Etapa: " + Math.min(etapa, 3) + "/3   |   Inimigos derrotados: " + inimigosDerrotados);
        inventoryModel.clear();
        for (ItemSelecionado i : itens()) {
            if (i.arma != null) {
                Artefato artefato = mochila.consultarAssociados().get(i.arma);
                inventoryModel.addElement("ARMA  " + i.arma.getNome() + " | " + i.arma.getCategoria() + " | dano " + i.arma.getDano(mochila) + (artefato == null ? "" : " | + " + artefato.getNome()));
            } else if (i.artefato != null) inventoryModel.addElement("ARTEFATO  " + i.artefato.getNome() + " | +" + i.artefato.getAumento());
            else inventoryModel.addElement("POÇÃO  Cura " + i.pocao.getCura() + "%");
        }
    }

    private void limparFormulario() { itemName.setText(""); itemValue.setText(""); }
    private void registrar(String mensagem) { log.append("• " + mensagem + "\n\n"); log.setCaretPosition(log.getDocument().getLength()); }
    private void aviso(String mensagem) { JOptionPane.showMessageDialog(this, mensagem, "Atenção", JOptionPane.INFORMATION_MESSAGE); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameGUI().setVisible(true));
    }
}
