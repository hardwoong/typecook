import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

// GamePanel을 확장하여 에드워드 리 게임 모드를 구현한 클래스
public class LeeGame extends GamePanel {

    private String playerName; // 플레이어 이름
    private String birthDate; // 플레이어 생년월일

    public LeeGame(String playerName, String birthDate, String difficulty) {
        // 부모 클래스 GamePanel의 생성자 호출 (LeeTextSource 사용)
        super(new LeeTextSource(), new ScorePanel(), difficulty);

        this.playerName = playerName; // 플레이어 이름 설정
        this.birthDate = birthDate; // 플레이어 생년월일 설정

        // 게임 초기화
        init();

        // 점수 표시 및 툴바 생성
        makeTop();

        // 우측 하트 및 버튼 생성
        makeHeartAndBtn();
    }

    // 게임 초기화 메서드
    private void init() {
        playWav("../Lee/Lee.wav"); // 초기 선택 사운드 재생

        // 게임 프레임 설정
        frame = new JFrame("Lee Game - " + playerName + " (난이도: " + difficulty + ")");
        frame.setSize(700, 600); // 창 크기 설정
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 창 닫으면 프로그램 종료
        frame.setLayout(new BorderLayout()); // 레이아웃 설정

        // 배경 이미지 설정
        backgroundLabel.setIcon(new ImageIcon("../Lee/LeeBack.png")); // 초기 배경 이미지
        backgroundLabel.setVisible(true);
        backgroundLabel.setHorizontalAlignment(SwingConstants.CENTER); // 수평 중앙 정렬
        backgroundLabel.setVerticalAlignment(SwingConstants.CENTER); // 수직 중앙 정렬
        frame.add(this, BorderLayout.CENTER); // 현재 패널을 프레임에 추가

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
        initialTimer.setRepeats(false); // 타이머 1회만 실행
        initialTimer.start(); // 게임 자동 실행
    }

    // 조건을 갖추어 성공 화면 보여주는 함수
    @Override
    protected void showSuccessBackground() {
        super.showSuccessBackground(); // 떨어지는 라벨 지우고 게임 정지

        backgroundLabel.setIcon(new ImageIcon("../Lee/LeeRes.png")); // 임시 성공 배경 이미지 표시
        revalidate(); // 재검토
        repaint(); // 다시 그리기

        // 2.5초 후 최종 성공 이미지 표시
        Timer successTimer = new Timer(2500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 2.5초 기다리고 난 후
                backgroundLabel.setIcon(new ImageIcon("../Lee/LeeSuc.png")); // 최종 성공 이미지
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

        // 임시 실패 배경 이미지 표시
        backgroundLabel.setIcon(new ImageIcon("../Lee/LeeRes.png"));
        revalidate(); // 재검토
        repaint(); // 다시 그리기

        // 2.5초 후 최종 실패 이미지 표시
        Timer failTimer = new Timer(2500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 2.5초 후
                backgroundLabel.setIcon(new ImageIcon("../Lee/LeeFail.png")); // 최종 실패 이미지
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
            showFailureBackground(); // 게임 실패 처리
        } else {
            // 임시 실패 배경 이미지 설정
            backgroundLabel.setIcon(new ImageIcon("../Lee/LeeMiss.png"));
            revalidate(); // 재검토
            repaint(); // 다시 그리기

            // 0.5초간 임시 실패 배경에서 머무르기
            Timer failTimer = new Timer(500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 0.5초 후
                    backgroundLabel.setIcon(new ImageIcon("../Lee/LeeBack.png")); // 원래 배경 복구
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