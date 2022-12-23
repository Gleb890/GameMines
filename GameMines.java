import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.Timer;

class GameMines extends JFrame{ // появление окна на экран

    final String TITLE_OF_PROGRAM = "Mines"; // заголовок
    final String SIGN_OF_FLAG = "f"; // знак флага
    final int BLOCK_SIZE = 30; // размер блока
    final int FIELD_SIZE = 9; // размер поля
    final int FIELD_DX = 6; // вертикаль
    final int FIELD_DY = 28 + 17; // горизонталь
    final int START_LOCATION = 200; // размер окна 200 на 200 пикселей
    final int MOUSE_BUTTON_LEFT = 1; // константа, возвращающая при нажатии клавиши мыши
    final int MOUSE_BUTTON_RIGHT = 3; // for управление мышкой
    final int NUMBER_OF_MINES = 10;
    final int[] COLOR_OF_NUMBERS = {0x0000FF, 0x008000, 0xFF0000, 0x800000, 0x0}; // массив цветов
    Cell[][] field = new Cell[FIELD_SIZE][FIELD_SIZE]; // двухмерный массив
    Random random = new Random();
    int countOpenedCells; // переменная, хранящая кол-во ячеек
    boolean youWon, bangMine; // переменная, когда мы выиграли, вторая - если попали на мину
    int bangX, bangY; // переменная, сохраняющая координаты взрыва

    public static void main(String[] args){
        new GameMines();
    }

    GameMines(){
        setTitle(TITLE_OF_PROGRAM); // заголовок программы
        setDefaultCloseOperation(EXIT_ON_CLOSE); // останавливает выполнение программы, когда закрываем ее
        setBounds(START_LOCATION, START_LOCATION, FIELD_SIZE * BLOCK_SIZE + FIELD_DX, FIELD_SIZE * BLOCK_SIZE + FIELD_DY);
        // устанавливает стартовую позицию и размеры программы
        setResizable(false); // масштабирование
        final TimerLabel timeLabel = new TimerLabel(); // панель, на которой будет отображаться игра
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        final Canvas canvas = new Canvas();
        canvas.setBackground(Color.white);
        canvas.addMouseListener(new MouseAdapter(){ // метод обеспечивает нажание клавиши мыши
            @Override
            public void mouseReleased(MouseEvent e){ // переопределяем данный обьект и назначаем клавишу, которая будет отвечать за задействование мыши
                super.mouseReleased(e);
                int x = e.getX()/BLOCK_SIZE; // абсолютная координата, которая отвечает за клик в x или y
                int y = e.getY()/BLOCK_SIZE;
                if (!bangMine && !youWon){
                    if (e.getButton() == MOUSE_BUTTON_LEFT) // нажата левая кнопка мыши
                        if (field[y][x].isNotOpen()){ // если данная ячейка не открыта
                            openCells(x, y); // открытие
                            youWon = countOpenedCells == FIELD_SIZE*FIELD_SIZE - NUMBER_OF_MINES; // если открыл все ячейки, то выигрыш
                            if (bangMine){ // если взорвалась мина
                                bangX = x;
                                bangY = y;
                            }
                        }
                    if (e.getButton() == MOUSE_BUTTON_RIGHT) field[y][x].inverseFlag(); // если нажата правая кпопка, то снимается флаг
                    if (bangMine || youWon) timeLabel.stopTimer(); // проигрыш
                    canvas.repaint(); // изменение конвы
                }
            }
        });
        add(BorderLayout.CENTER, canvas); // делает конву по центру и время
        add(BorderLayout.SOUTH, timeLabel);
        setVisible(true);
        initField();
    }

    void openCells(int x, int y){ // рекурсивная процедура открытия ячеек
        if (x < 0 || x > FIELD_SIZE - 1 || y < 0 || y > FIELD_SIZE - 1) return; // неверные координаты
        if (!field[y][x].isNotOpen()) return; // ячейка уже открыта
        field[y][x].open();
        if (field[y][x].getCountBomb() > 0 || bangMine) return; // проверка на то, что ячейка не пустая
        for (int dx = -1; dx < 2; dx++)
            for (int dy = -1; dy < 2; dy++) openCells(x + dx, y + dy);
    }

    void initField(){ // инициализирует игровое поле
        // проходит по вложенному циклу и создает каждую клетку
        int x, y, countMines = 0;
        for (x = 0; x < FIELD_SIZE; x++)
            for (y = 0; y < FIELD_SIZE; y++)
                field[y][x] = new Cell();
        // генерирует мины
        while (countMines < NUMBER_OF_MINES){
            do {
                x = random.nextInt(FIELD_SIZE);
                y = random.nextInt(FIELD_SIZE);
            } while (field[y][x].isMined());
            field[y][x].mine();
            countMines++;
        }
        // подсчет опасных ячеек
        for (x = 0; x < FIELD_SIZE; x++)
            for (y = 0; y < FIELD_SIZE; y++)
                if (!field[y][x].isMined()){
                    int count = 0;
                    for (int dx = -1; dx < 2; dx++)
                        for (int dy = -1; dy < 2; dy++){
                            int nX = x + dx;
                            int nY = y + dy;
                            if (nX < 0 || nY < 0 || nX > FIELD_SIZE - 1 || nY > FIELD_SIZE - 1){
                                nX = x;
                                nY = y;
                            }
                            count += (field[nY][nX].isMined()) ? 1 : 0;
                        }
                    field[y][x].setCountBomb(count);
                }
    }

    class Cell{ // игровое поле ячейки
        private int countBombNear;
        private boolean isOpen, isMine, isFlag;

        void open(){
            isOpen = true;
            bangMine = isMine;
            if (!isMine) countOpenedCells++;
        }

        void mine() {isMine = true;}

        void setCountBomb(int count) {countBombNear = count;}

        int getCountBomb() {return countBombNear;}

        boolean isNotOpen() {return !isOpen;}

        boolean isMined() {return isMine;}

        void inverseFlag() {isFlag = !isFlag;}

        void paintBomb(Graphics g, int x, int y, Color color){
            g.setColor(color);
            g.fillRect(x*BLOCK_SIZE + 7, y*BLOCK_SIZE + 10, 18, 10);
            g.fillRect(x*BLOCK_SIZE + 11, y*BLOCK_SIZE + 6, 10, 18);
            g.fillRect(x*BLOCK_SIZE + 9, y*BLOCK_SIZE + 8, 14, 14);
            g.setColor(Color.white);
            g.fillRect(x*BLOCK_SIZE + 11, y*BLOCK_SIZE + 10, 4, 4);
        }

        void paintString(Graphics g, String str, int x, int y, Color color){
            g.setColor(color);
            g.setFont(new Font("", Font.BOLD, BLOCK_SIZE));
            g.drawString(str, x*BLOCK_SIZE + 8, y*BLOCK_SIZE + 26);
        }

        void paint(Graphics g, int x, int y){
            g.setColor(Color.lightGray);
            g.drawRect(x*BLOCK_SIZE, y*BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
            if (!isOpen){
                if ((bangMine || youWon) && isMine) paintBomb(g, x, y, Color.black);
                else {
                    g.setColor(Color.lightGray);
                    g.fill3DRect(x*BLOCK_SIZE, y*BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE, true);
                    if (isFlag) paintString(g, SIGN_OF_FLAG, x, y, Color.red);
                }
            } else
            if (isMine) paintBomb(g, x, y, bangMine? Color.red : Color.black);
            else
            if (countBombNear > 0)
                paintString(g, Integer.toString(countBombNear), x, y, new Color(COLOR_OF_NUMBERS[countBombNear - 1]));
        }
    }

    class TimerLabel extends JLabel{ // таймер
        Timer timer = new Timer();

        TimerLabel() {timer.scheduleAtFixedRate(timerTask, 0, 1000);} // период таймера

        TimerTask timerTask = new TimerTask(){
            volatile int time; // изменение времени
            Runnable refresher = new Runnable(){ // счетчик обновления времени
                public void run(){
                    TimerLabel.this.setText(String.format("%02d:%02d", time / 60, time % 60)); // отображение минут и секунд
                }
            };
            public void run(){ //
                time++;
                SwingUtilities.invokeLater(refresher); // постоянное обновление времени
            }
        };

        void stopTimer() {timer.cancel();} // остановка таймера
    }

    class Canvas extends JPanel{ // конва для рисования
        @Override
        public void paint(Graphics g){
            super.paint(g);
            // вложенные циклы
            for (int x = 0; x < FIELD_SIZE; x++)
                for (int y = 0; y < FIELD_SIZE; y++) field[y][x].paint(g, x, y); // отрисовка программы на экране
        }
    }
}