
import java.awt.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// 각 캐릭터 게임을 위한 부모 패널
public class GamePanel extends JPanel {
    // 텍스트 관련 요소
    protected TextSource textSource; // 단어를 제공하는 소스
    protected JTextField text = new JTextField(20); // 단어 입력 필드

    // 점수 및 게임 상태 관련 요소
    protected ScorePanel scorePanel; // 점수를 관리하는 패널
    protected String difficulty; // 게임 난이도
    protected boolean isRunning = false; // 게임 실행 상태 플래그
    protected Random random = new Random(); // 랜덤 위치 설정을 위한 객체

    // 게임 요소
    protected JLabel backgroundLabel; // 배경 이미지 라벨
    protected JPanel inputPanel; // 입력 패널
    protected JButton selectCharacterButton; // 캐릭터 선택 버튼
    protected JButton homeButton; // 홈 화면 이동 버튼
    protected JLabel scoreLabel; // 점수를 표시할 라벨
    protected JLabel timerLabel; // 타이머를 표시할 라벨

    // 게임 로직 관련 변수
    protected List<FallingLabel> fallingLabels = new ArrayList<>(); // 화면에 떨어지는 단어 라벨 리스트
    protected FallThread fallThread; // 단어가 떨어지는 것을 관리하는 스레드
    protected TimerThread timerThread; // 게임 시간 타이머 스레드
    protected double elapsedTime = 0.0; // 경과 시간 (초 단위)

    // 하트 요소
    protected JLabel[] hearts = new JLabel[3]; // 하트 이미지 배열
    protected int heartIndex = 0; // 하트 인덱스

    // 배경음악 관련 요소
    protected MusicThread musicThread; // 배경음악 스레드

    // 결과 저장 파일
    protected static final String FILE_PATH = "game_results.txt";

    // 플레이어 정보
    protected String playerName; // 플레이어 이름
    protected String birthDate; // 플레이어 생년월일

    protected JFrame frame; // 메인 프레임

    public GamePanel(TextSource textSource, ScorePanel scorePanel, String difficulty) {
        // 전달받은 값으로 변수 초기화 및 저장
        this.textSource = textSource;
        this.scorePanel = scorePanel;
        this.difficulty = difficulty;

        // 배치 조정 및 배경 설정
        init();

        // 입력 패널 설정
        initializeInputPanel();

        // 배경 음악 초기화 및 재생
        initializeBackgroundMusic("../bgm.wav");
    }

    private void init() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));

        // 배경 이미지 설정
        backgroundLabel = new JLabel();
        backgroundLabel.setLayout(null);
        add(backgroundLabel, BorderLayout.CENTER);
    }

    // 폴링 라벨 관리 스레드 클래스
    class FallThread extends Thread {
        private GamePanel gamePanel; // GamePanel 참조
        private int delay; // 단어가 떨어지는 딜레이 시간 (ms)
        private boolean running = true; // 스레드 실행 여부

        public FallThread(GamePanel gamePanel, int delay) {
            this.gamePanel = gamePanel;
            this.delay = delay;
        }

        public void stopFalling() {
            running = false; // 스레드 종료 플래그
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(delay); // 딜레이 만큼 대기
                    if (!running)
                        break; // 스레드가 멈춘 상태면 종료
                    gamePanel.updateLabel(); // 화면 업데이트 호출
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break; // 인터럽트 발생 시 안전하게 종료
                }
            }
        }
    }

    // 게임 시작 함수
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

        startTimerThread(); // 타이머 스레드 시작
    }

    // 게임 종료 함수
    protected void stopGame() {
        isRunning = false; // 게임 종료
        if (fallThread != null) { // 떨어지는 스레드가 작동중이면
            fallThread.stopFalling(); // 스레드 멈추는 함수 호출
        }
        stopTimerThread(); // 타이머 쓰레드 정지
    }

    // 단어가 화면에 맞게 떨어지게 하는 함수
    synchronized protected void updateLabel() {
        newWord(); // 새로운 단어 생성
        List<FallingLabel> labelsToRemove = new ArrayList<>();
        for (int i = 0; i < fallingLabels.size(); i++) {
            FallingLabel label = fallingLabels.get(i);
            label.fall(); // 라벨 떨어지기 시작
            if (label.getY() > getHeight()) { // 화면 밖으로 나가면
                labelsToRemove.add(label); // 삭제할 라벨에 해당 라벨 추가하고
                scorePanel.decrease(10); // 10점 감소
                showTemporaryFailBackground(); // 오답 배경 출력
            }
        }
        for (int i = 0; i < labelsToRemove.size(); i++) {
            FallingLabel label = labelsToRemove.get(i); // 삭제할 라벨들
            backgroundLabel.remove(label); // 배경에서 지우고
            fallingLabels.remove(label); // 폴링라벨 배열에서 지움
        }
        checkScore(); // 점수 확인

        backgroundLabel.revalidate(); // 배경 재검토
        backgroundLabel.repaint(); // 배경 다시 그리기
    }

    private void initializeInputPanel() {
        // 인풋 패널 액션 리스너 설정
        text.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inWord = text.getText(); // 플레이어 입력 값 전달
                if (inWord.length() == 0) // 아무 것도 입력 안했을 때 반환
                    return;

                boolean found = false;
                for (int i = 0; i < fallingLabels.size(); i++) {
                    FallingLabel label = fallingLabels.get(i);
                    if (label.getText().equals(inWord)) { // 정답일 때
                        scorePanel.increase(10); // 10점 증가
                        text.setText(""); // 인풋 패널 초기화
                        label.setVisible(false); // 맞춰진 라벨 보이지 않게
                        backgroundLabel.remove(label); // 배경에서 삭제
                        fallingLabels.remove(label); // 폴링 라벨 배열에서 삭제
                        found = true; // 정답을 맞췄다는 플래그
                        break; // 하나 처리되면 후 탈출
                    }
                }

                if (!found) { // 오답일 때
                    scorePanel.decrease(10); // 10점 감소
                    showTemporaryFailBackground(); // 오답 사진 출력 함수
                    text.setText(""); // 인풋 패널 초기화
                }

                checkScore(); // 점수 확인 및 상태 업데이트
                repaint(); // 변경 반영하여 다시 그리기
            }
        });

        inputPanel = new JPanel() { // 인풋 패널 생성
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // 그라데이션 설정 (왼쪽은 검정, 오른쪽은 흰색)
                GradientPaint gradient = new GradientPaint(0, 0, Color.BLACK, getWidth(), 0, Color.WHITE);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight()); // 패널에 그라데이션 채우기
            }
        };

        inputPanel.setLayout(new BorderLayout()); // 패널 레이아웃을 BorderLayout으로 설정
        inputPanel.setPreferredSize(new Dimension(600, 50)); // 입력 패널의 가로 크기 설정

        // 텍스트 필드 스타일링
        text.setFont(new Font("Serif", Font.PLAIN, 18)); // 고전적인 글꼴
        text.setForeground(Color.WHITE); // 흰색 텍스트
        text.setBackground(Color.DARK_GRAY); // 짙은 회색 배경
        text.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2)); // 연한 회색 테두리
        text.setHorizontalAlignment(JTextField.CENTER); // 수평 중앙

        // 텍스트 필드를 중앙에 배치
        inputPanel.add(text, BorderLayout.CENTER);

        // 제출 버튼 추가
        JButton submitButton = new JButton("✔"); // 버튼 텍스트를 체크마크로 설정
        submitButton.setFont(new Font("SansSerif", Font.BOLD, 18)); // 폰트를 SansSerif로 설정
        submitButton.setForeground(Color.WHITE); // 텍스트 색상을 흰색으로 설정
        submitButton.setBackground(Color.GRAY); // 버튼의 배경색을 회색으로 설정
        submitButton.setFocusPainted(false); // 클릭 시 테두리 포커스를 제거
        submitButton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2)); // 테두리를 짙은 회색으로 설정
        // 버튼 클릭 시 인풋 패널 액션 이벤트 발생
        submitButton.addActionListener(e -> {
            String input = text.getText();
            if (!input.isEmpty()) { // 비어 있지 않으면
                text.postActionEvent(); // 텍스트 필드 액션 발생
            }
        });

        // 버튼을 텍스트 필드 오른쪽에 배치
        inputPanel.add(submitButton, BorderLayout.EAST);

        // 패널을 아래쪽에 추가
        add(inputPanel, BorderLayout.SOUTH);
    }

    // 새 단어 추가하는 함수
    protected void newWord() {
        // x 좌표값 랜덤
        int randomX = random.nextInt(backgroundLabel.getWidth() - 100);
        // 새 폴링라벨을 추가
        FallingLabel newLabel = new FallingLabel(textSource.get(), randomX, 10);
        fallingLabels.add(newLabel);
        backgroundLabel.add(newLabel);
        newLabel.setVisible(true);
        repaint(); // 다시 그리기
    }

    // 점수를 확인하고 게임 상태를 업데이트하는 함수
    protected void checkScore() {
        int score = scorePanel.getScore(); // 현재 점수 확인
        scoreLabel.setText("점수: " + score); // 점수값 업데이트

        if (score >= 100) { // 점수가 100점 이상이면
            showSuccessBackground(); // 성공 배경 표시
        } else if (score <= -30) { // 점수가 -30점 이하면
            showFailureBackground(); // 실패 배경 표시
        }
    }

    // 오답을 통해 일시적 실패 배경을 보여주는 함수
    protected void showTemporaryFailBackground() {
        if (heartIndex < hearts.length) { // 하트가 남아 있다면
            hearts[heartIndex].setIcon(new ImageIcon("../brokenHeart.png")); // 현재 하트를 깨진 하트로 변경
            heartIndex++; // 다음 하트를 대상으로 이동
        }
        // 이후는 자손 클래스에서 구현
    }

    // 조건을 갖추어 성공 화면을 출력하는 함수
    protected void showSuccessBackground() {
        hideFallingLabels(); // 떨어지는 라벨 제거
        stopGame(); // 게임 정지
        // 이후는 자손 클래스에서 구현
    }

    // 조건을 갖추어 실패 화면을 출력하는 함수
    protected void showFailureBackground() {
        hideFallingLabels(); // 떨어지는 라벨 제거
        stopGame(); // 게임 정지
        // 이후는 자손 클래스에서 구현
    }

    // 화면에 떨어지는 단어 클래스
    protected class FallingLabel extends JLabel {
        private int x, y;

        public FallingLabel(String text, int x, int y) {
            super(text); // 지정 파일에서 단어를 가져와서
            this.x = x;
            this.y = y;
            setSize(100, 30);
            setLocation(x, y); // 지정된 크기로 자정된 좌표 값에서 생성
            setForeground(Color.WHITE); // 흰색으로 설정
        }

        public void fall() {
            y += 10; // 점점 떨어짐
            setLocation(x, y); // 떨어진 y값으로 재설정
        }
    }

    // 상단에 붙일 버튼들 생성
    protected void makeTop() {
        // 상단 패널
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout()); // 점수와 툴바를 나란히 배치
        frame.add(topPanel, BorderLayout.NORTH); // 메인 프레임 상단에 배치

        // 점수 표시를 위한 JLabel 설정
        scoreLabel = new JLabel("점수: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20)); // 점수 텍스트 폰트 설정
        scoreLabel.setForeground(Color.BLACK); // 텍스트 색상 설정
        scoreLabel.setHorizontalAlignment(SwingConstants.RIGHT); // 텍스트를 우측 정렬
        topPanel.add(scoreLabel, BorderLayout.EAST); // 점수를 우측에 배치

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
    }

    // 하트 이미지와 하트 아래 배경음악 제어 버튼 생성 함수
    protected void makeHeartAndBtn() {
        JPanel heartPanel = new JPanel();
        heartPanel.setLayout(new BoxLayout(heartPanel, BoxLayout.Y_AXIS)); // 하트를 세로로 나열
        heartPanel.setOpaque(false); // 투명 배경 설정
        for (int i = 0; i < hearts.length; i++) {
            hearts[i] = new JLabel(new ImageIcon("../heart.png")); // 초기 하트 이미지
            heartPanel.add(hearts[i]);
        }

        // 배경음악 제어 버튼 추가
        JButton playButton = new JButton(new ImageIcon("../play.png"));
        playButton.setBorder(BorderFactory.createEmptyBorder()); // 테두리 X
        playButton.setContentAreaFilled(false); // 투명
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resumeBackgroundMusic(); // 배경음악 재개하는 함수 액션 등록
            }
        });

        heartPanel.add(playButton);

        JButton stopMusicButton = new JButton(new ImageIcon("../stop.png"));
        stopMusicButton.setBorder(BorderFactory.createEmptyBorder()); // 테두리 X
        stopMusicButton.setContentAreaFilled(false); // 투명
        stopMusicButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseBackgroundMusic(); // 배경음악 정지하는 함수 액션 등록
            }
        });

        heartPanel.add(stopMusicButton);

        frame.add(heartPanel, BorderLayout.EAST); // 하트 패널을 우측에 추가
    }

    //
    class TimerThread extends Thread {
        private boolean isRunning; // 타이머 실행 여부 플래그
        private JLabel timerLabel; // 타이머 시간을 표시할 JLabel 컴포넌트

        // TimerThread 생성자
        public TimerThread(JLabel timerLabel) {
            this.isRunning = true; // 타이머 실행 상태 초기화
            this.timerLabel = timerLabel; // 타이머를 업데이트할 JLabel 참조 저장
        }

        // 타이머를 멈추는 함수
        public void stopTimer() {
            this.isRunning = false; // 타이머 실행 플래그를 false로 설정
        }

        @Override
        public void run() {
            while (isRunning) { // 실행 상태가 true일 때만 반복 실행
                try {
                    Thread.sleep(10); // 10ms 간격으로 타이머 업데이트
                    elapsedTime += 0.01; // 경과 시간 누적 (10ms -> 0.01초)
                    DecimalFormat df = new DecimalFormat("0.00"); // 소수점 두 자리 포맷 지정
                    String formattedTime = "시간: " + df.format(elapsedTime) + "초"; // 포맷된 시간 문자열 생성

                    updateTime(formattedTime); // 화면에 타이머 갱신
                } catch (InterruptedException e) {
                    return; // 인터럽트 발생 시 타이머 종료
                }
            }
        }
    }

    // 타이머 레이블에 갱신된 시간 문자열을 표시
    synchronized protected void updateTime(String formattedTime) {
        timerLabel.setText(formattedTime); // JLabel에 포맷된 시간 설정
    }

    // 타이머 쓰레드 시작
    protected void startTimerThread() {
        timerThread = new TimerThread(timerLabel); // TimerThread 객체 생성
        timerThread.start(); // 타이머 쓰레드 실행
    }

    // 타이머 스레드 정지
    protected void stopTimerThread() {
        if (timerThread != null) {
            timerThread.stopTimer(); // 타이머 쓰레드의 실행 상태를 false로 설정
        }
    }

    // 화면에 떨어지는 모든 라벨을 숨기고 제거하는 함수
    protected void hideFallingLabels() {
        for (int i = 0; i < fallingLabels.size(); i++) { // 모든 라벨 반복
            FallingLabel label = fallingLabels.get(i);
            backgroundLabel.remove(label); // 배경에서 해당 라벨 제거
        }
        fallingLabels.clear(); // 리스트 비우기
        backgroundLabel.revalidate(); // 배경 재검토
        backgroundLabel.repaint(); // 배경 다시 그리기
    }

    public class MusicThread extends Thread {
        private String musicFilePath; // 음악 파일 경로
        private Clip musicClip; // 음악 재생용 Clip 객체
        private boolean isPaused = false; // 음악 일시정지 상태 확인 플래그

        // MusicThread 생성자
        public MusicThread(String musicFilePath) {
            this.musicFilePath = musicFilePath; // 음악 파일 경로 저장
        }

        @Override
        public void run() {
            try {
                File musicFile = new File(musicFilePath); // 오디오 파일 객체 생성
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile); // 오디오 파일 읽어 오디오 스트림으로 변환
                Clip musicClip = AudioSystem.getClip(); // 오디오 데이터를 메모리에 로드하고 재생할 Clip 객체 생성
                musicClip.open(audioStream); // 오디오 스트림을 열어 Clip에 연결
                musicClip.start(); // 오디오 재생
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                // 지원하지 않는 파일이나 읽기 도충 문제가 생기거나 사용 불가한 리소스일때 에러 출력
                System.err.println("음악 파일 재생 오류: " + e.getMessage());
            }
        }

        // 음악 일시정지 함수
        public void pauseMusic() {
            if (musicClip != null && musicClip.isRunning()) { // 음악이 재생 중이라면
                musicClip.stop(); // 음악 정지
                isPaused = true; // 일시정지 상태 플래그 설정
            }
        }

        // 음악 재개 함수
        public void resumeMusic() {
            if (musicClip != null && isPaused) { // 일시정지 상태라면
                musicClip.start(); // 음악 재생 재개
                isPaused = false; // 일시정지 상태 해제
            }
        }

        // 음악 정지 함수
        protected void stopMusic() {
            if (musicClip != null) { // 음악 클립이 존재하면
                musicClip.stop(); // 음악 정지
                musicClip.close(); // 리소스 해제
            }
        }

        // 배경음악 실행 초기화 함수
        protected void initializeBackgroundMusic(String musicFilePath) {
            musicThread = new MusicThread(musicFilePath);
            musicThread.start(); // 새로운 배경 음악 스레드 실행
        }
    }

    protected void initializeBackgroundMusic(String musicFilePath) {
        musicThread = new MusicThread(musicFilePath);
        musicThread.start(); // 배경 음악 스레드 실행
    }

    protected void pauseBackgroundMusic() {
        if (musicThread != null) {
            musicThread.pauseMusic(); // 배경 음악 일시정지
        }
    }

    protected void resumeBackgroundMusic() {
        if (musicThread != null) {
            musicThread.resumeMusic(); // 음악 재개
        }
    }

    protected void stopBackgroundMusic() {
        if (musicThread != null) {
            musicThread.stopMusic(); // 음악 정지
        }
    }

    // 배경 음악을 재생할 함수
    protected void playWav(String musicFilePath) {
        try {
            File musicFile = new File(musicFilePath); // 오디오 파일 객체 생성
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile); // 오디오 파일 읽어 오디오 스트림으로 변환
            Clip musicClip = AudioSystem.getClip(); // 오디오 데이터를 메모리에 로드하고 재생할 Clip 객체 생성
            musicClip.open(audioStream); // 오디오 스트림을 열어 Clip에 연결
            musicClip.start(); // 오디오 재생
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            // 지원하지 않는 파일이나 읽기 도충 문제가 생기거나 사용 불가한 리소스일때 에러 출력
            System.err.println("음악 파일 재생 오류: " + e.getMessage());
        }
    }
}
