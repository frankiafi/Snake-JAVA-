package it.unimol.snake;

import javax.swing.*;

public class GameLabel extends JLabel {
    /* Un'implementazione dell'interfaccia Icon che crea le icone dalle it.unimol.snake.immagini */
    private final ImageIcon imgSnake, imgSfondo, imgTestaSnake, imgCibo;

    /* Istanziamento delle icone utili al programma */
    public GameLabel() {
        imgSfondo = new ImageIcon(getClass().getResource("/it/unimol/snake/immagini/sfondo.png"));
        imgSnake = new ImageIcon(getClass().getResource("/it/unimol/snake/immagini/snake.png"));
        imgTestaSnake = new ImageIcon(getClass().getResource("/it/unimol/snake/immagini/testaSnake.png"));
        imgCibo = new ImageIcon(getClass().getResource("/it/unimol/snake/immagini/cibo.png"));
    }

    /* Queste funzioni creano l'immagine usata dall'icona */
    public void setSfondo() {
        this.setIcon(imgSfondo);
    }

    public void setSnake() {
        this.setIcon(imgSnake);
    }

    public void setTestaSnake() {
        this.setIcon(imgTestaSnake);
    }

    public void setCibo() {
        this.setIcon(imgCibo);
    }
}
