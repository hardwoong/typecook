import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// GamePanel을 상속받아 평가절하 게임 모드를 구현한 클래스
public class PyungGame extends GamePanel {
    private String playerName; // 플레이어 이름
    private String birthDate; // 플레이어 생년월일

    private FallThread fallThread; // 단어 떨어뜨리는 쓰레드
    private GaugeThread gaugeThread; // 게이지 자동 감소 쓰레드
    private SuccessFailThread successFailThread; // 성공/실패 조건 체크 쓰레드

    private int redGaugeValue = 50; // 빨간색 게이지 초기값
    private int blueGaugeValue = 50; // 파란색 게이지 초기값

    private CustomGauge redGauge; // 빨간색 게이지 UI 컴포넌트
    private CustomGauge blueGauge; // 파란색 게이지 UI 컴포넌트

    public PyungGame(String playerName, String birthDate, String difficulty) {
        super(new TextSource(), new ScorePanel(), difficulty); // 부모 클래스(GamePanel) 생성자 호출

        this.playerName = playerName; // 플레이어 이름 설정
        this.birthDate = birthDate; // 플레이어 생년월일 설정

        // 프레임 및 초기화 작업
        init();

        // 게이지바, 툴바 패널 생성
        makeTop();

        // 우측 하트 및 음악 제어 버튼 패널 생성
        makeHeartAndBtn();
    }

    // 커스텀 게이지 컴포넌트
    class CustomGauge extends JLabel {
        private Color gaugeColor; // 게이지 색상
        private int value = 50; // 현재 값 (0~100)

        public CustomGauge(Color gaugeColor) {
            this.gaugeColor = gaugeColor;
            setPreferredSize(new Dimension(200, 20)); // 기본 크기 설정
        }

        public void setValue(int value) {
            this.value = Math.max(0, Math.min(100, value)); // 값은 0~100 사이로 제한
            repaint(); // 게이지를 다시 그리기
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // 배경색 흰색
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());

            // 게이지 색상 (현재 값에 따라 너비 설정)
            g.setColor(gaugeColor);
            int filledWidth = (int) (getWidth() * (value / 100.0)); // 비율에 따른 채워진 너비 계산
            g.fillRect(0, 0, filledWidth, getHeight());

            // 테두리 그리기
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }
    }

    // 프레임 및 초기화 작업
    private void init() {
        playWav("../Pyung/Pyung.wav"); // 초기 선택 사운드 재생

        frame = new JFrame("Pyung Game - " + playerName + " (난이도: " + difficulty + ")");
        frame.setSize(700, 600); // 게임 창 크기 설정
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 닫기 버튼 클릭 시 프로그램 종료
        frame.setLayout(new BorderLayout()); // 레이아웃 설정

        // 초기 배경 이미지 설정
        backgroundLabel.setIcon(new ImageIcon("../Pyung/PyungBack.png"));
        backgroundLabel.setVisible(true); // 배경 이미지를 보이도록 설정
        backgroundLabel.setHorizontalAlignment(SwingConstants.CENTER); // 수평 중앙 정렬
        backgroundLabel.setVerticalAlignment(SwingConstants.CENTER); // 수직 중앙 정렬
        frame.add(this, BorderLayout.CENTER); // GamePanel(현재 클래스)을 프레임에 추가

        frame.revalidate(); // 화면 재검토
        frame.repaint(); // 화면 다시 그리기
        frame.setVisible(true); // 게임 창 표시

        // 500ms 지연 후 게임 시작
        Timer initialTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                revalidate(); // 재검토
                repaint(); // 다시 그리기
                startGame(); // 게임 시작
            }
        });

        // ActionListener 오버라이딩 (빨간 라벨, 파란 라벨 분류 위해)
        text.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inWord = text.getText(); // 플레이어 입력값 가져오기
                if (inWord.length() == 0) // 빈 값이면 반환
                    return;

                boolean found = false; // 정답 여부 플래그 초기값
                List<FallingLabel> labelsToRemove = new ArrayList<>();

                // 입력된 단어가 떨어지는 라벨과 매칭되는지 확인
                for (int i = 0; i < fallingLabels.size(); i++) {
                    FallingLabel label = fallingLabels.get(i);
                    if (label.getText().equals(inWord)) { // 정답 입력시
                        if (label instanceof RedFallingLabel) { // 빨간 라벨이면
                            // 빨간 게이지 증가
                            redGaugeValue = Math.min(100, redGaugeValue + 10);
                            redGauge.setValue(redGaugeValue); // 게이지 업데이트
                        } else if (label instanceof BlueFallingLabel) { // 파란 라벨이면
                            // 파란 게이지 증가
                            blueGaugeValue = Math.min(100, blueGaugeValue + 10);
                            blueGauge.setValue(blueGaugeValue); // 게이지 업데이트
                        }

                        // 라벨 제거를 위한 리스트에 추가
                        labelsToRemove.add(label);
                        found = true; // 정답 여부 플래그 저장
                        break; // 하나 찾고 탈출
                    }
                }

                // 라벨 제거
                for (int i = 0; i < labelsToRemove.size(); i++) {
                    FallingLabel label = labelsToRemove.get(i);
                    backgroundLabel.remove(label); // 화면에서 제거
                    fallingLabels.remove(label); // 리스트에서 제거
                }

                if (!found) { // 단어를 맞추지 못한 경우
                    showTemporaryFailBackground(); // 임시 실패 함수 호출
                }

                text.setText(""); // 입력란 초기화
                backgroundLabel.revalidate(); // 화면 재검토
                backgroundLabel.repaint(); // 화면 다시 그리기
            }
        });

        initialTimer.setRepeats(false); // 타이머 1회만 실행
        initialTimer.start(); // 게임 자동 실행
        gaugeThread = new GaugeThread(this); // 게이지 스레드 생성
        gaugeThread.start(); // 게이지 스레드 시작
        successFailThread = new SuccessFailThread(this); // 성공 실패 체크 스레드 생성
        successFailThread.start(); // 성공 실패 체크 스레드 시작
    }

    @Override
    protected void makeTop() {
        // 상단 패널
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout()); // 점수와 툴바를 나란히 배치
        frame.add(topPanel, BorderLayout.NORTH); // 메인 프레임 상단에 배치

        // 타이머 표시를 위한 JLabel 생성
        timerLabel = new JLabel("시간: 0.00초");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 20)); // 폰트 설정
        timerLabel.setForeground(Color.BLACK); // 텍스트 색상 설정
        timerLabel.setHorizontalAlignment(SwingConstants.LEFT); // 좌측 정렬

        topPanel.add(timerLabel, BorderLayout.CENTER); // 타이머를 중앙에 배치

        frame.add(topPanel, BorderLayout.NORTH); // 점수 라벨과 시간 라벨 상단에 배치

        // 툴바 추가 (게임 시작 및 정지 버튼)
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false); // 툴바 이동 불가 설정

        JButton startButton = new JButton("Start"); // 멈춰있는 게임을 재개할 버튼
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame(); // 게임 재개
                text.setEnabled(true); // 입력 필드 활성화
                text.setForeground(Color.BLACK); // 활성화 상태에서 텍스트 색상 설정
                text.setBackground(Color.WHITE); // 활성화 상태에서 배경 색상 설정
            }
        });

        toolBar.add(startButton); // 시작 버튼 추가

        JButton stopButton = new JButton("Stop"); // 진행중인 게임을 정지시킬 버튼
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopGame(); // 게임 정지
                text.setEnabled(false); // 입력 필드 비활성화
                text.setDisabledTextColor(Color.GRAY); // 비활성화 상태에서 텍스트 색상 설정
                text.setBackground(Color.LIGHT_GRAY); // 비활성화 상태에서 배경 색상 설정
            }
        });
        toolBar.add(stopButton); // 정지 버튼 추가

        // 캐릭터 선택창 버튼
        selectCharacterButton = new JButton("캐릭터 선택");
        selectCharacterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopGame(); // 캐릭터 선택창으로 나가며 게임 중단
                new SelectCharacter(playerName, birthDate, difficulty);
                frame.dispose(); // 현재 창 닫기
                stopBackgroundMusic(); // 배경음악 중지
            }
        });

        toolBar.add(selectCharacterButton);

        homeButton = new JButton("처음 화면");
        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopGame(); // 처음 화면으로 나가며 게임 중단
                new StartFrame(); // 초기 화면 클래스 호출
                frame.dispose(); // 현재 창 닫기
                stopBackgroundMusic(); // 배경음악 중지
            }
        });
        toolBar.add(homeButton); // 처음 화면 버튼 추가

        topPanel.add(toolBar, BorderLayout.NORTH); // 툴바를 좌측에 배치

        // 커스텀 게이지 설정 (빨간 게이지, 파란 게이지 포함할)
        JPanel gaugePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        redGauge = new CustomGauge(Color.RED); // 빨간 게이지 생성
        blueGauge = new CustomGauge(Color.BLUE); // 파란 게이지 생성

        // 게이지 초기값 설정
        redGauge.setValue(redGaugeValue);
        blueGauge.setValue(blueGaugeValue);

        // gaugePanel에 빨간 게이지, 파란 게이지 추가
        gaugePanel.add(redGauge);
        gaugePanel.add(blueGauge);

        topPanel.add(gaugePanel, BorderLayout.SOUTH); // 상단 패널 아래에 게이지 패널 추가
        frame.add(topPanel, BorderLayout.NORTH); // 마지막으로 상단 패널을 프레임 상단에 추가
    }

    // PyungGame의 빨간색과 파란색 게이지를 주기적으로 감소시키는 스레드 클래스
    class GaugeThread extends Thread {
        private PyungGame pyungGame; // PyungGame 참조를 통해 게이지 값 업데이트
        private boolean running; // 스레드 실행 여부를 나타내는 플래그

        public GaugeThread(PyungGame pyungGame) {
            this.pyungGame = pyungGame; // PyungGame 객체 참조
            this.running = true; // 스레드 실행 상태를 true로 초기화
        }

        // 스레드를 종료하기 위한 함수
        public void stopGauge() {
            running = false; // 스레드 실행 플래그를 false로 설정
            this.interrupt(); // 안전하게 스레드를 종료하기 위해 인터럽트 발생
        }

        @Override
        public void run() {
            try {
                // 스레드 실행 루프
                while (running) {
                    Thread.sleep(1000); // 1초마다 게이지 값을 감소시키기 위해 대기

                    // 현재 빨간색과 파란색 게이지 값을 가져오기
                    int redGaugeValue = pyungGame.getRedGaugeValue(); // 빨간 게이지 값
                    int blueGaugeValue = pyungGame.getBlueGaugeValue(); // 파란 게이지 값

                    // 게이지 값을 2씩 감소 (최소 0까지 감소)
                    redGaugeValue = Math.max(0, redGaugeValue - 2); // 0 이하로 내려가지 않도록 제한
                    blueGaugeValue = Math.max(0, blueGaugeValue - 2); // 0 이하로 내려가지 않도록 제한

                    // PyungGame의 게이지 값을 업데이트
                    pyungGame.updateGaugeValues(redGaugeValue, blueGaugeValue);
                }
            } catch (InterruptedException e) {
                // 스레드가 인터럽트될 경우 안전하게 종료
                Thread.currentThread().interrupt(); // 인터럽트 상태 복구
                return; // 안전하게 종료
            }
        }
    }

    // 게이지 값을 업데이트하는 함수
    public synchronized void updateGaugeValues(int redValue, int blueValue) {
        redGaugeValue = redValue; // 빨간 게이지 값을 업데이트
        blueGaugeValue = blueValue; // 파란 게이지 값을 업데이트

        redGauge.setValue(redGaugeValue); // 빨간 게이지 값 갱신
        blueGauge.setValue(blueGaugeValue); // 파란 게이지 값 갱신
    }

    // 빨간색으로 표시되는 FallingLabel의 하위 클래스
    private class RedFallingLabel extends FallingLabel {
        public RedFallingLabel(int x, int y) {
            super(textSource.get(), x, y); // 부모 클래스 FallingLabel 생성자 호출
            setForeground(Color.RED); // 글자색을 빨간색으로 설정
        }

        // 정답 입력 시 성공 처리 메서드
        public void handleSuccess() {
            // 빨간 게이지 값에 10을 더하고, 최대 100까지만 유지
            redGaugeValue = Math.min(100, redGaugeValue + 10);
            redGauge.setValue(redGaugeValue); // UI에 반영된 게이지 값 업데이트
        }
    }

    // 파란색으로 표시되는 FallingLabel의 하위 클래스
    private class BlueFallingLabel extends FallingLabel {
        public BlueFallingLabel(int x, int y) {
            super(textSource.get(), x, y); // 부모 클래스 FallingLabel 생성자 호출
            setForeground(Color.BLUE); // 글자색을 파란색으로 설정
        }

        // 정답 입력 시 성공 처리 메서드
        public void handleSuccess() {
            // 파란 게이지 값에 10을 더하고, 최대 100까지만 유지
            blueGaugeValue = Math.min(100, blueGaugeValue + 10);
            blueGauge.setValue(blueGaugeValue); // UI에 반영된 게이지 값 업데이트
        }
    }

    class FallThread extends Thread {
        private GamePanel gamePanel; // GamePanel 참조
        private int delay; // 단어가 떨어지는 딜레이 시간 (ms)
        private boolean running = true; // 스레드 실행 여부

        public FallThread(GamePanel gamePanel, int delay) {
            this.gamePanel = gamePanel; // 게임 패널 참조 저장
            this.delay = delay; // 딜레이 시간 설정
        }

        // 스레드 종료 함수
        public void stopFalling() {
            running = false; // 스레드 실행 플래그를 false로 설정
        }

        @Override
        public void run() {
            while (running) { // running이 true일 동안 계속 실행
                try {
                    Thread.sleep(delay); // 설정된 딜레이 시간 동안 스레드 대기
                    if (!running) // 중단 플래그가 설정되면 스레드 종료
                        return; // 스레드가 멈춘 상태면 종료
                    gamePanel.updateLabel(); // 단어를 화면에 업데이트 (떨어뜨림)
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break; // 인터럽트 발생 시 안전하게 종료
                }
            }
        }
    }

    // 단어를 생성하고 화면에 떨어지는 동작을 처리하는 함수
    synchronized protected void updateLabel() {
        newWord(); // 새로운 단어 생성 및 화면에 추가

        List<FallingLabel> labelsToRemove = new ArrayList<>(); // 제거할 라벨들을 저장할 리스트

        // 떨어지는 라벨을 하나씩 업데이트
        for (int i = 0; i < fallingLabels.size(); i++) {
            FallingLabel label = fallingLabels.get(i);
            label.fall(); // 라벨을 아래로 떨어뜨리는 함수 호출

            // 라벨이 화면 아래로 떨어졌는지 확인
            if (label.getY() > getHeight()) {
                labelsToRemove.add(label); // 화면 밖으로 벗어난 라벨을 제거 리스트에 추가
                scorePanel.decrease(10); // 점수 10점 감소
                showTemporaryFailBackground(); // 오답 배경 표시
            }
        }

        // 화면에서 제거할 라벨 처리
        for (int i = 0; i < labelsToRemove.size(); i++) {
            FallingLabel label = labelsToRemove.get(i);
            backgroundLabel.remove(label); // 화면에서 라벨 제거
            fallingLabels.remove(label); // 라벨 리스트에서 제거
        }

        backgroundLabel.revalidate(); // 화면 재검토
        backgroundLabel.repaint(); // 화면 다시 그리기
    }

    // 폴링 라벨 상속으로 인한 newWord 오버라이딩
    @Override
    protected void newWord() {
        // x 좌표값 랜덤
        int randomX = random.nextInt(backgroundLabel.getWidth() - 100);
        // 새 폴링라벨을 추가
        FallingLabel newLabel = null;
        boolean isRed = random.nextBoolean(); // 빨간색 또는 파란색 라벨 결정

        if (isRed) { // 랜덤값이 빨간 색 참이면
            newLabel = new RedFallingLabel(randomX, 10); // 빨간 폴링라벨 추가
        } else { // 파란색이면
            newLabel = new BlueFallingLabel(randomX, 10); // 파란 폴링라벨 추가
        }

        fallingLabels.add(newLabel);
        backgroundLabel.add(newLabel);
        newLabel.setVisible(true);

        backgroundLabel.revalidate(); // 화면 재검토
        backgroundLabel.repaint(); // 화면 다시 그리기
    }

    // 게임 시작 함수 오버라이딩
    @Override
    protected void startGame() {
        isRunning = true; // 게임 진행 중
        int delay;
        switch (difficulty) {
            case "Easy":
                delay = 800;
                break;
            case "Medium":
                delay = 600;
                break;
            case "Hard":
                delay = 400;
                break;
            default:
                return;
        } // 플레이어가 입력한 난이도에 따라 delay값 설정
        fallThread = new FallThread(this, delay);
        fallThread.start(); // 단어 떨어지는 스레드 시작

        startTimerThread(); // 타이머 쓰레드 시작
    }

    public int getRedGaugeValue() { // 빨간 게이지 값 반환 함수
        return redGaugeValue;
    }

    public int getBlueGaugeValue() { // 파란 게이지 값 반환 함수
        return blueGaugeValue;
    }

    // 게임의 성공 또는 실패 상태를 체크하는 스레드 클래스
    class SuccessFailThread extends Thread {
        private PyungGame game; // PyungGame 객체 참조
        private boolean running; // 스레드 실행 여부를 제어하는 플래그
        private int successTime; // 성공 조건이 연속으로 만족된 시간 (초 단위)
        private int failureTime; // 실패 조건이 연속으로 만족된 시간 (초 단위)

        public SuccessFailThread(PyungGame game) {
            this.game = game; // PyungGame 객체 참조 저장
            this.running = true; // 스레드 실행 상태를 true로 초기화
        }

        // 스레드 종료 메서드
        public void stopCheck() {
            this.running = false; // 스레드 실행을 중단
            this.interrupt(); // 스레드에 인터럽트 발생
        }

        @Override
        public void run() {
            try {
                while (running) { // running 플래그가 true일 동안 반복 실행
                    Thread.sleep(1000); // 1초 동안 대기

                    // PyungGame에서 빨간 게이지와 파란 게이지의 현재 값을 가져옴
                    int redGaugeValue = game.getRedGaugeValue();
                    int blueGaugeValue = game.getBlueGaugeValue();

                    // 성공 조건 확인 (빨간 게이지와 파란 게이지가 모두 60 이상일 때)
                    if (redGaugeValue >= 60 && blueGaugeValue >= 60) {
                        successTime++; // 성공 시간 증가
                        failureTime = 0; // 실패 시간 초기화

                        // 성공 상태가 3초 연속 유지되면 성공 처리
                        if (successTime >= 3) {
                            game.showSuccessBackground(); // 성공 배경 표시
                            return; // 스레드 종료
                        }
                    }
                    // 실패 조건 확인 (빨간 게이지 또는 파란 게이지가 20 이하일 때)
                    else if (redGaugeValue <= 20 || blueGaugeValue <= 20) {
                        failureTime++; // 실패 시간 증가
                        successTime = 0; // 성공 시간 초기화

                        // 실패 상태가 3초 연속 유지되면 실패 처리
                        if (failureTime >= 3) {
                            game.showFailureBackground(); // 실패 배경 표시
                            return; // 스레드 종료
                        }
                    }
                    // 성공 및 실패 조건을 모두 만족하지 않을 때
                    else {
                        successTime = 0; // 성공 시간 초기화
                        failureTime = 0; // 실패 시간 초기화
                    }
                }
            } catch (InterruptedException e) {
                // 스레드가 인터럽트되면 안전하게 종료
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @Override
    public void stopGame() {
        if (fallThread != null) { // 떨어지는 스레드가 작동중이면
            fallThread.stopFalling(); // 스레드 멈추는 함수 호출
        }
        if (gaugeThread != null && gaugeThread.isAlive()) { // 게이지 스레드가 작동중이면
            gaugeThread.stopGauge(); // 스레드 멈추는 함수 호출
        }
        if (successFailThread != null && successFailThread.isAlive()) // 성공실패체크 스레드가 작동중이면
            successFailThread.stopCheck(); // 스레드 멈추는 함수 호출
        stopTimerThread(); // 타이머 쓰레드 정지
    }

    // 성공 화면 표시 메서드
    @Override
    protected void showSuccessBackground() {
        SwingUtilities.invokeLater(() -> { // UI 스레드에서 안전하게 실행
            hideFallingLabels(); // 화면에 표시된 떨어지는 단어 라벨 제거
            stopGame(); // 게임 정지 (스레드 및 타이머 종료)

            // 임시 성공 배경 이미지 설정
            backgroundLabel.setIcon(new ImageIcon("../Pyung/PyungRes.png"));
            revalidate(); // 레이아웃 갱신
            repaint(); // 화면 다시 그리기

            // 2.5초 후 최종 성공 화면을 표시
            Timer successTimer = new Timer(2500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    playWav("../Pyung/PyungSuc.wav"); // 성공 사운드 재생
                    backgroundLabel.setIcon(new ImageIcon("../Pyung/PyungSuc.png")); // 최종 성공 배경 이미지 설정
                    revalidate(); // 레이아웃 갱신
                    repaint(); // 화면 다시 그리기
                    saveGameResult(); // 게임 결과 저장 (파일에 기록)
                }
            });
            successTimer.setRepeats(false); // 타이머를 한 번만 실행하도록 설정
            successTimer.start(); // 타이머 시작
        });
    }

    // 실패 화면 표시 메서드
    @Override
    protected void showFailureBackground() {
        SwingUtilities.invokeLater(() -> { // UI 스레드에서 안전하게 실행
            hideFallingLabels(); // 화면에 표시된 떨어지는 단어 라벨 제거
            stopGame(); // 게임 정지 (스레드 및 타이머 종료)

            // 임시 실패 배경 이미지 설정
            backgroundLabel.setIcon(new ImageIcon("../Pyung/PyungRes.png"));
            revalidate(); // 레이아웃 갱신
            repaint(); // 화면 다시 그리기

            // 2.5초 후 최종 실패 화면을 표시
            Timer failTimer = new Timer(2500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    playWav("../Pyung/PyungFail.wav"); // 실패 사운드 재생
                    backgroundLabel.setIcon(new ImageIcon("../Pyung/PyungFail.png")); // 최종 실패 배경 이미지 설정
                    revalidate(); // 레이아웃 갱신
                    repaint(); // 화면 다시 그리기
                }
            });
            failTimer.setRepeats(false); // 타이머를 한 번만 실행하도록 설정
            failTimer.start(); // 타이머 시작
        });
    }

    // 일시적 실패 화면 표시 메서드
    @Override
    protected void showTemporaryFailBackground() {
        super.showTemporaryFailBackground(); // 기본 실패 처리 (하트 감소 등)

        // 모든 하트가 소진되었을 경우
        if (heartIndex >= hearts.length) {
            showFailureBackground(); // 실패 화면 표시
        } else {
            // 임시 실패 배경 이미지 설정
            backgroundLabel.setIcon(new ImageIcon("../Pyung/PyungMiss.png"));
            revalidate(); // 레이아웃 갱신
            repaint(); // 화면 다시 그리기

            // 0.5초 후 원래 배경 이미지로 복구
            Timer failTimer = new Timer(500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    backgroundLabel.setIcon(new ImageIcon("../Pyung/PyungBack.png")); // 원래 배경 이미지로 설정
                    revalidate(); // 레이아웃 갱신
                    repaint(); // 화면 다시 그리기
                }
            });
            failTimer.setRepeats(false); // 타이머를 한 번만 실행하도록 설정
            failTimer.start(); // 타이머 시작
        }
    }

    private void saveGameResult() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            String timeText = timerLabel.getText().split(":")[1].trim(); // "시간: 12.34초"에서 숫자만 추출
            String timeWithoutUnit = timeText.replace("초", "").trim(); // 초 단위 제거

            // 결과를 파일에 저장
            writer.write(playerName + ", " + birthDate + ", " + timeWithoutUnit + "\n");
            // 저장 성공 시 기록 알림
            JOptionPane.showMessageDialog(null, "게임 결과가 저장되었습니다!", "알림", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            // 저장 실패 시 오류 알림
            JOptionPane.showMessageDialog(null, "결과 저장 중 오류가 발생했습니다!", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}
