package it.unimol.snake;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;

public class GameFrame extends JFrame {

    private boolean su, giu, destra, sinistra, cibo, gameOn;
    private final int CELLE = 35;
    private final int FACILE = 0;
    private final int MEDIA = 1;
    private final int DIFFICILE = 2;
    private final int[] vCol, vRow;
    private int i, j, n, dxCibo, dyCibo, punteggio, dxTesta, dyTesta;
    private final GameLabel[][] matrix;
    private final JLabel punti;
    private JLabel record;
    private final JButton btnStart;
    private final JComboBox difficolta;
    private Scheduler sc;


    public GameFrame() {
        vCol = new int[CELLE];
        vRow = new int[CELLE];
        matrix = new GameLabel[CELLE][CELLE];

        /* Creazione del pulsante "Nuova Partita", dell'indicatore dei punti e del record e della lista
         * per la scelta del livello di difficoltà. Istanziamento, inoltre, del pannello da visualizzare */
        JPanel panel = new JPanel();
        difficolta = new JComboBox();
        difficolta.addItem("Facile");
        difficolta.addItem("Media");
        difficolta.addItem("Difficile");
        difficolta.setEditable(false); //Rende non modificabile dall'utente la lista creata
        btnStart = new JButton("Nuova Partita");
        punti = new JLabel("-");
        // Caricamento del record in base alla difficoltà impostata
        if (difficolta.getSelectedIndex() == FACILE)
            record = new JLabel(String.valueOf(caricaRecordFacile()));
        else if (difficolta.getSelectedIndex() == MEDIA)
            record = new JLabel(String.valueOf(caricaRecordMedia()));
        else if (difficolta.getSelectedIndex() == DIFFICILE)
            record = new JLabel(String.valueOf(caricaRecordDifficile()));

        /* Istanziamento delle icone di punteggio e record con aggiustamenti grafici */
        JLabel iconaPunti = new JLabel();
        iconaPunti.setIcon(new ImageIcon(getClass().getResource("/it/unimol/snake/immagini/punteggio.png")));
        iconaPunti.setBorder(BorderFactory.createEmptyBorder(0, 45, 0, 5));
        JLabel iconaRecord = new JLabel();
        iconaRecord.setIcon(new ImageIcon(getClass().getResource("/it/unimol/snake/immagini/record.png")));
        iconaRecord.setBorder(BorderFactory.createEmptyBorder(0, 45, 0, 5));

        /* Aggiunta delle componenti istanziate al pannello grafico */
        panel.add(difficolta);
        panel.add(btnStart);
        panel.add(iconaPunti);
        panel.add(punti);
        panel.add(iconaRecord);
        panel.add(record);

        /* La classe "GridBagLayout" inserisce le componenti in una griglia di rettangoli diversi tra loro,
         * mentre la classe "GridBagConstraints" specifica i vincoli per le componenti disposte secondo
         * l'utilizzo di "GridBagLayout" */
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints lim = new GridBagConstraints();

        /* Creazione del pannello rappresentante l'area di gioco */
        JPanel panel2 = new JPanel();
        panel2.setBorder(BorderFactory.createLineBorder(new Color(255, 95, 15), 3));
        panel2.setLayout(layout); /* In questo modo il pannello seguirà le regole di "GridBagLayout" */

        /* Ciclo per il setting dello sfondo nel riquadro riguardante l'area di gioco */
        for (i = 0; i < CELLE; i++) {
            lim.gridy = i;
            for (j = 0; j < CELLE; j++) {
                matrix[i][j] = new GameLabel();
                matrix[i][j].setSfondo();
                lim.gridx = j;
                layout.setConstraints(matrix[i][j], lim);
                panel2.add(matrix[i][j]);
            }
        }

        GridBagLayout layout2 = new GridBagLayout();
        GridBagConstraints lim2 = new GridBagConstraints();

        /* Creazione contenitore per i pannelli precedentemente creati */
        Container c = this.getContentPane();
        c.setLayout(layout2);

        /* Aggiunta del primo pannello al contenitore con i relativi vincoli */
        lim2.gridx = lim2.gridy = 0;
        lim2.insets.bottom = 10;
        lim2.fill = GridBagConstraints.BOTH;
        layout2.setConstraints(panel, lim2);
        c.add(panel);

        /* Aggiunta del secondo pannello con i relativi vincoli */
        lim2.gridx = 0;
        lim2.gridy = 1;
        layout2.setConstraints(panel2, lim2);
        c.add(panel2);
        c.setBackground(Color.DARK_GRAY);

        /* La funzione seguente è utilizzata per lo spostamento dello snake. In particolare, la funzione prende in
         * input l'azione eseguita dall'utente da tastiera (es. pressione della freccia su, ecc...)
         * e verifica che sia una delle azioni possibili. Una volta trovata l'azione corrispondente,
         * verrà eseguito un controllo che permetterà, se possibile, di spostare lo snake nella direzione selezionata.
         * In caso contrario non accadrà nulla e lo snake continuerà nella direzione seguita precedentemente. */
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (destra || sinistra) {
                        giu = true;
                        su = sinistra = destra = false;
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (destra || sinistra) {
                        su = true;
                        giu = destra = sinistra = false;
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if (su || giu) {
                        destra = true;
                        su = giu = sinistra = false;
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    if (su || giu) {
                        sinistra = true;
                        destra = giu = su = false;
                    }
                }
            }
        });

        /* Questa funzione si occupa invece degli input dal mouse. In particolare verifica se il gioco
         * è in corso o meno e, grazie al click del mouse sul bottone "start", avvia il gioco in caso di
         * partita non ancora avviata. */
        btnStart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
                int velocita = 0; // Variabile per stabilire la velocità di movimento dello snake

                /* All'interno di questo ciclo viene controllata la selezione della difficoltà da parte dell'utente.
                 * In base alla scelta effettuata verrà impostata la velocità dello snake */
                if (!gameOn) {
                    switch (difficolta.getSelectedIndex()) {
                        case FACILE:
                            velocita = 120;
                            break;
                        case MEDIA:
                            velocita = 90;
                            break;
                        case DIFFICILE:
                            velocita = 60;
                            break;
                    }
                    initGriglia();
                    sc = new Scheduler(velocita);
                    sc.start();
                    gameOn = true;
                }
            }
        });

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); /* Chiude il gioco quando viene premuto il pulsante
                                                              * di chiusura */
        this.setResizable(false); // Rende la schermata non ridimensionabile
        this.setVisible(true); // Necessario alla visualizzazione del pannello
        this.setBounds((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2) - (500 / 2),
                (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2) - (580 / 2),
                500, 580); /* I primi due campi definiscono la posizione del pannello nello spazio
                                        * dello schermo. Gli ultimi due rappresentano la larghezza e l'altezza
                                        * dello stesso */
    }

    public void initGriglia() {
        gameOn = false; // Diventerà "true" una volta iniziata la partita
        sinistra = su = giu = false; // Movimenti dello snake che diventeranno "true" quando necessario
        destra = true; // All'avvio della partita lo snake inizierà muovendosi verso destra
        n = 7; // Questa variabile imposta la lunghezza iniziale dello snake
        punteggio = 0; // Punteggio iniziale
        cibo = false; // In questo modo non comparirà cibo sullo schermo
        dxCibo = dyCibo = 0; // Coordinate per la generazione del cibo

        // Posizionamento iniziale della testa dello snake
        dxTesta = dyTesta = (CELLE/2);
        matrix[dxTesta][dyTesta].setTestaSnake();

        //Posizionamento iniziale del resto dello snake
        for (i = 1; i < n; i++) {
            vRow[i] = dxTesta;
            vCol[i] = (dyTesta - i);
            matrix[dxTesta][dyTesta-i].setSnake();
        }

        punti.setText("0"); // Imposta il punteggio iniziale a video

        //Caricamento del record in base alla difficoltà scelta
        if (difficolta.getSelectedIndex() == FACILE)
            record.setText(String.valueOf(caricaRecordFacile()));
        else if (difficolta.getSelectedIndex() == MEDIA)
            record.setText(String.valueOf(caricaRecordMedia()));
        else if (difficolta.getSelectedIndex() == DIFFICILE)
            record.setText(String.valueOf(caricaRecordDifficile()));
    }

    public void generaCibo() {
        boolean flag = false;
        int x = 0, y = 0, z;

        while (!flag) {
            x = (int) (Math.random() * CELLE); // Math.random restituisce un numero compreso tra 0 e 1(escluso)
            y = (int) (Math.random() * CELLE);

            /* Questo controllo fa si che il cibo non venga generato in una casella occupata
            da un pezzo dello snake. Se le coordinate x e y coincidono con una posizione
            in cui si trova un pezzo di snake il flag resta impostato su "false" e la funzione cerca
            una nuova posizione. Altrimenti il ciclo viene terminato e vengono impostate le coordinate
            per la generazione di nuovo cibo. */
            z = 0;
            while (z < n) {
                if ((vRow[z] == x) && (vCol[z] == y)) {
                    z = (n + 1);
                    flag = false;
                } else {
                    z++;
                    flag = true;
                }
            }
        }
        matrix[x][y].setCibo();
        dxCibo = x;
        dyCibo = y;
    }

    private int caricaRecordFacile(){
        int t = 0;

        try (BufferedReader fin = new BufferedReader(new FileReader(getClass().
                getResource("/it/unimol/snake/file/recordFacile.txt").getFile()))) {
            t = Integer.parseInt(fin.readLine()); // Legge la stringa presente nel file e la trasforma in un intero
        } catch (IOException ex) {
            System.out.println("ERRORE: " + ex.getMessage());
        }
        return t; // Ritorna il record letto
    }

    private int caricaRecordMedia(){
        int t = 0;

        try (BufferedReader fin = new BufferedReader(new FileReader(getClass().
                getResource("/it/unimol/snake/file/recordMedia.txt").getFile()))) {
            t = Integer.parseInt(fin.readLine());
        } catch (IOException ex) {
            System.out.println("ERRORE: " + ex.getMessage());
        }
        return t;
    }

    private int caricaRecordDifficile(){
        int t = 0;

        try (BufferedReader fin = new BufferedReader(new FileReader(getClass().
                getResource("/it/unimol/snake/file/recordDifficile.txt").getFile()))) {
            t = Integer.parseInt(fin.readLine());
        } catch (IOException ex) {
            System.out.println("ERRORE: " + ex.getMessage());
        }
        return t;
    }

    private void scriviNuovoRecordFacile(int record){
        try(PrintWriter fout = new PrintWriter(new FileWriter(getClass().
                getResource("/it/unimol/snake/file/recordFacile.txt").getFile()))){
            fout.println(record); // Scrive su file il record preso in input
        } catch(IOException ex){
            System.out.println("ERRORE: " + ex.getMessage());
        }
    }

    private void scriviNuovoRecordMedia(int record){
        try(PrintWriter fout = new PrintWriter(new FileWriter(getClass().
                getResource("/it/unimol/snake/file/recordMedia.txt").getFile()))){
            fout.println(record);
        } catch(IOException ex){
            System.out.println("ERRORE: " + ex.getMessage());
        }
    }

    private void scriviNuovoRecordDifficile(int record){
        try(PrintWriter fout = new PrintWriter(new FileWriter(getClass().
                getResource("/it/unimol/snake/file/recordDifficile.txt").getFile()))){
            fout.println(record);
        } catch(IOException ex){
            System.out.println("ERRORE: " + ex.getMessage());
        }
    }

    class Scheduler implements Runnable{
        private final Thread thread;
        private final int velocita;

        public Scheduler(int velocita){
            this.velocita = velocita;
            this.thread = new Thread(this);
        }

        public void start(){
            this.thread.start();
        }

        public void stop(){
            int p = Integer.parseInt(punti.getText());

            /* Verifica il livello di difficoltà e, in caso di nuovo record, salva il punteggio ottenuto su file
             * e visualizza un messaggio a video, altrimenti visualizza messaggio di partita finita. */
            if(difficolta.getSelectedIndex() == FACILE){
                if(p>caricaRecordFacile()){
                    scriviNuovoRecordFacile(p);
                    JOptionPane.showMessageDialog(null, "Complimenti!\n" +
                            "Hai fatto segnare un nuovo record per la modalità 'Facile'!\n" +
                                    "Hai totalizzato " + p + " punti.", "CONGRATULAZIONI",
                            JOptionPane.INFORMATION_MESSAGE);
                } else{
                    JOptionPane.showMessageDialog(null, "GAME OVER!\n" +
                                    "Hai totalizzato " + p + " punti.", "FINE PARTITA",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else if(difficolta.getSelectedIndex() == MEDIA){
                if(p>caricaRecordMedia()){
                    scriviNuovoRecordMedia(p);
                    JOptionPane.showMessageDialog(null, "Complimenti!\n" +
                                    "Hai fatto segnare un nuovo record per la modalità 'Media'!\n" +
                                    "Hai totalizzato " + p + " punti.", "CONGRATULAZIONI",
                            JOptionPane.INFORMATION_MESSAGE);
                } else{
                    JOptionPane.showMessageDialog(null, "GAME OVER!\n" +
                                    "Hai totalizzato " + p + " punti.", "FINE PARTITA",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else if(difficolta.getSelectedIndex() == DIFFICILE){
                if(p>caricaRecordDifficile()){
                    scriviNuovoRecordDifficile(p);
                    JOptionPane.showMessageDialog(null, "Complimenti!\n" +
                                    "Hai fatto segnare un nuovo record per la modalità 'Difficile'!\n" +
                                    "Hai totalizzato " + p + " punti.", "CONGRATULAZIONI",
                            JOptionPane.INFORMATION_MESSAGE);
                } else{
                    JOptionPane.showMessageDialog(null, "GAME OVER!\n" +
                                    "Hai totalizzato " + p + " punti.", "FINE PARTITA",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }

            // Viene poi resettato lo sfondo per l'inizio di un'eventuale nuova partita
            for(i=0;i<CELLE;i++){
                vCol[i] = vRow[i] = 0;
                for(j=0;j<CELLE;j++){
                    matrix[i][j].setSfondo();
                }
            }
        }

        /* Funzione che gestisce le operazioni di gioco a partita in corso */
        @Override
        public void run(){
            boolean go = true;
            boolean flag;

            while(go){
                if(!cibo){ // Se non è presente cibo questo viene generato
                    generaCibo();
                    cibo = true;
                }

                if((dxCibo == dxTesta)&&(dyTesta == dyCibo)){/* Se le due uguaglianze sono "true" allora il cibo
                                                    * è stato mangiato dallo snake. */
                    punti.setText(String.valueOf(++punteggio)); // Per cui il punteggio viene aumentato di 1.
                    cibo = false; // Il cibo viene impostato su "false" di modo che possa essere nuovamente generato
                    n++; // La lunghezza dello snake aumenta di 1
                }

                // Resetta lo sfondo bianco dove lo snake è passato
                matrix[vRow[n-1]][vCol[n-1]].setSfondo();
                // Rimuove effettivamente il pezzo di snake dalla casella ormai diventata bianca
                for(i=(n-1); i>0; i--){
                    vRow[i] = vRow[i-1];
                    vCol[i] = vCol[i-1];
                }

                /* Queste operazioni invece si occupano dello spostamento del corpo dello snake in avanti */
                vRow[1] = dxTesta;
                vCol[1] = dyTesta;
                matrix[dxTesta][dyTesta].setSnake();

                /* Queste operazioni permettono inoltre allo snake di muoversi da una "parete"
                 * all'altra (es. uscita dalla parete di destra, entrata dalla parete di sinistra) */
                if(destra){
                    dyTesta = (dyTesta+1)%CELLE;
                } else if(sinistra){
                    if(dyTesta>0)
                        dyTesta = (dyTesta-1);
                    else
                        dyTesta = (CELLE-1);
                } else if(su){
                    if(dxTesta>0)
                        dxTesta = (dxTesta-1);
                    else
                        dxTesta = (CELLE-1);
                }else if(giu){
                    dxTesta = (dxTesta+1)%CELLE;
                }

                // Posizionamento della testa dello snake in base all'input inserito
                vRow[0] = dxTesta;
                vCol[0] = dyTesta;
                matrix[dxTesta][dyTesta].setTestaSnake();

                /* Questo ciclo controlla se lo snake ha toccato un pezzo di se stesso e, se così fosse,
                 * termina la partita. */
                i = 1;
                flag = false;
                while(i<n && !flag){
                    if((vRow[0] == vRow[i])&&(vCol[0] == vCol[i]))
                        flag = true;
                    else
                        i++;
                }

                if(flag)
                    go = false;

                try{
                    Thread.sleep(velocita); // Funzione che regola la velocità di movimento dello snake
                } catch(InterruptedException ex){
                    System.err.println("ERRORE: " + ex.getMessage());
                }
            }
            gameOn = false;
            stop();
        }
    }

}
