package src;

import java.util.concurrent.ThreadLocalRandom;

public class Pocao {
    protected int cura;
    protected int peso;

    public Pocao() {
        this.cura = ThreadLocalRandom.current().nextInt(25, 36);
        this.peso = 1;
    }

    public int getCura() { return this.cura; }
    public int getPeso() { return this.peso; }

    public void editarCura(int cura) {
        if (cura < 1 || cura > 100) throw new IllegalArgumentException("A cura deve ficar entre 1% e 100%.");
        this.cura = cura;
    }
}
