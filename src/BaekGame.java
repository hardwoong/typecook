import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;

// GamePanel을 상속받아 백종원 게임 모드를 구현한 클래스
public class BaekGame extends GamePanel {

    private String playerName; // 플레이어 이름
    private String birthDate; // 플레이어 생년월일

    private JPanel blockerPanel; // 방해 사각형 패널
    private BlockerThread blockerThread; // 방해 사각형 생성용 쓰레드

    public BaekGame(String playerName, String birthDate, String difficulty) {
        super(new TextSource(), new ScorePanel(), difficulty);

        this.playerName = playerName; // 플레이어 이름 설정
        this.birthDate = birthDate; // 플레이어 생년월일 설정

        // 프레임 및 초기화 작업
        init();

        // 점수 및 툴바 패널 생성
        makeTop();

        // 우측 하트 및 음악 제어 버튼 패널 생성
        makeHeartAndBtn();
    }

    // 프레임 및 초기화 작업
    private void init() {
        playWav("../Baek/Baek.wav"); // 초기 선택 사운드 재생

        frame = new JFrame("Baek Game - " + playerName + " (난이도: " + difficulty + ")");
        frame.setSize(700, 600); // 게임 창 크기 설정
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 닫기 버튼 클릭 시 프로그램 종료
        frame.setLayout(new BorderLayout()); // 레이아웃 설정

        // 초기 배경 이미지 설정
        backgroundLabel.setIcon(new ImageIcon("../Baek/BaekBack.png"));
        backgroundLabel.setVisible(true); // 배경 이미지를 보이도록 설정
        backgroundLabel.setHorizontalAlignment(SwingConstants.CENTER); // 수평 중앙 정렬
        backgroundLabel.setVerticalAlignment(SwingConstants.CENTER); // 수직 중앙 정렬
        frame.add(this, BorderLayout.CENTER); // 게임 패널을 프레임에 추가

        // 방해 요소 패널 생성 (기본 숨김)
        blockerPanel = new JPanel();
        blockerPanel.setOpaque(true);
        blockerPanel.setBackground(Color.BLACK);
        blockerPanel.setVisible(false); // 기본적으로 숨겨진 상태
        blockerPanel.setLayout(null);
        backgroundLabel.add(blockerPanel, JLayeredPane.POPUP_LAYER); // 방해 패널을 배경 위 레이어에 추가

        // 화면 갱신
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true); // 프레임을 화면에 표시

        // 방해 요소 스레드 시작
        blockerThread = new BlockerThread(blockerPanel, backgroundLabel);
        blockerThread.start();

        // 500ms 지연 후 게임 시작
        Timer initialTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                revalidate(); // 재검토
                repaint(); // 다시 그리기
                startGame(); // 게임 시작
            }
        });
        initialTimer.setRepeats(false); // 타이머 1회만 실행
        initialTimer.start(); // 게임 자동 실행
    }

    // 툴바에 버튼 추가 시 방해 요소 쓰레드를 중지
    @Override
    protected void makeTop() {
        super.makeTop();
        // 캐릭터 선택창 이동 버튼 누를 시
        selectCharacterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                blockerThread.stopBlocker(); // 방해 쓰레드 중지
            }
        });
        // 시작 화면 이동 버튼 누를 시
        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                blockerThread.stopBlocker(); // 방해 쓰레드 중지
            }
        });
    }

    // 방해 쓰레드 안전 종료
    private void stopBlockerThread() {
        if (blockerThread != null && blockerThread.isAlive()) {
            blockerThread.stopBlocker(); // 쓰레드 종료
        }
    }

    // 방해 요소 스레드 클래스 (4초마다 방해 패널 생성)
    public class BlockerThread extends Thread {
        private JPanel blockerPanel;
        private JLabel backgroundLabel;
        private boolean running = true;

        public BlockerThread(JPanel blockerPanel, JLabel backgroundLabel) {
            this.blockerPanel = blockerPanel;
            this.backgroundLabel = backgroundLabel;
        }

        public void stopBlocker() {
            running = false; // 실행 중지
            this.interrupt(); // 스레드 인터럽트
        }

        @Override
        public void run() {
            Random random = new Random();
            while (running) {
                try {
                    Thread.sleep(4000); // 4초마다 실행

                    // 랜덤 위치 설정
                    int randomY = random.nextInt(backgroundLabel.getHeight() / 2);
                    int blockerHeight = backgroundLabel.getHeight() / 2;
                    int blockerWidth = backgroundLabel.getWidth();

                    createBlocker(blockerPanel, randomY, blockerHeight, blockerWidth); // 방해 패널 표시

                    Thread.sleep(2000); // 2초 동안 유지

                    deleteBlocker(blockerPanel); // 방해 패널 숨김

                } catch (InterruptedException e) {
                    return; // 중단 시 안전하게 종료
                }
            }
        }
    }

    // 방해 패널 생성 메서드
    synchronized private void createBlocker(JPanel blockerPanel, int randomY, int blockerHeight, int blockerWidth) {
        blockerPanel.setBounds(0, randomY, blockerWidth, blockerHeight); // 방해 패널 위치와 크기 설정
        blockerPanel.setVisible(true); // 방해 패널 표시
        playWav("../Baek/Block.wav"); // 방해 사운드 재생
    }

    // 방해 패널 숨김 메서드
    synchronized private void deleteBlocker(JPanel blockerPanel) {
        blockerPanel.setVisible(false); // 방해 패널 숨김
    }

    @Override
    protected void stopGame() {
        super.stopGame(); // 기본 게임 종료 로직 호출
        stopBlockerThread(); // 방해 요소 스레드 종료
    }

    @Override
    protected void showSuccessBackground() {
        super.showSuccessBackground(); // 떨어지는 라벨 지우고 게임 정지

        backgroundLabel.setIcon(new ImageIcon("../Baek/BaekRes.png")); // 임시 결과 배경 이미지 설정
        revalidate(); // 재검토
        repaint(); // 다시 그리기
        // 2.5초 후 최종 성공 배경으로 변경 및 사운드 재생
        Timer successTimer = new Timer(2500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 2.5초 기다리고 난 후
                backgroundLabel.setIcon(new ImageIcon("../Baek/BaekSuc.png")); // 최종 성공 배경 이미지 설정
                revalidate(); // 재검토
                repaint(); // 다시 그리기

                saveGameResult(); // 게임 결과 저장
            }
        });
        successTimer.setRepeats(false); // 1번만 2.5초 기다리는 연산 하도록
        successTimer.start(); // 2.5초 기다리기 시작
    }

    // 조건을 갖추어 실패 화면 보여주는 함수
    @Override
    protected void showFailureBackground() {
        super.showFailureBackground(); // 떨어지는 라벨 지우고 게임 정지

        backgroundLabel.setIcon(new ImageIcon("../Baek/BaekRes.png")); // 임시 결과 배경 이미지 설정
        revalidate(); // 재검토
        repaint(); // 다시 그리기
        // 2.5초 후 최종 실패 배경으로 변경 및 사운드 재생
        Timer failTimer = new Timer(2500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backgroundLabel.setIcon(new ImageIcon("../Baek/BaekFail.png")); // 최종 실패 배경 이미지 설정
                revalidate(); // 재검토
                repaint(); // 다시 그리기
            }
        });
        failTimer.setRepeats(false); // 1번만 2.5초 기다리도록
        failTimer.start(); // 2.5초 기다리기 시작
    }

    // 오답 입력 시
    @Override
    protected void showTemporaryFailBackground() {
        super.showTemporaryFailBackground(); // 하트가 남아 있으면 하트 지우기

        if (heartIndex >= hearts.length) { // 모든 하트가 깨졌을 경우
            showFailureBackground(); // 실패 배경 표시
        } else { // 하트가 남았을 경우
            backgroundLabel.setIcon(new ImageIcon("../Baek/BaekMiss.png")); // 임시 실패 배경 이미지 설정
            revalidate(); // 재검토
            repaint(); // 다시 그리기
            // 0.5초간 임시 실패 배경에서 머무르기
            Timer failTimer = new Timer(500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 0.5초 후
                    backgroundLabel.setIcon(new ImageIcon("../Baek/BaekBack.png")); // 이전 배경 이미지로 복구
                    revalidate(); // 재검토
                    repaint(); // 다시 그리기
                }
            });
            failTimer.setRepeats(false); // 1번만 0.5초 기다리도록
            failTimer.start(); // 0.5초 기다리기 (임시 실패 배경 유지)
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