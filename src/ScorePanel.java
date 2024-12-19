import javax.swing.*;
import java.awt.*;

// ScorePanel 클래스: 점수를 표시하고 관리하는 패널
public class ScorePanel extends JPanel {
    private int score = 0; // 현재 점수를 저장하는 변수
    private JLabel textLabel = new JLabel("점수"); // 점수 라벨
    private JLabel scoreLabel = new JLabel(Integer.toString(score)); // 점수값을 표시하는 라벨

    // 생성자: 점수 패널의 기본 UI 설정
    public ScorePanel() {
        // ScorePanel 초기화 함수
        initScorePanel();
    }

    private void initScorePanel() {
        this.setBackground(Color.YELLOW); // 배경 색상 설정 (노란색)
        setLayout(null); // 레이아웃 매니저 해제 (절대 위치 사용)

        // "점수" 라벨 설정
        textLabel.setSize(50, 20); // 라벨의 크기 설정 (가로, 세로)
        textLabel.setLocation(10, 10); // 라벨의 위치 설정 (x, y 좌표)
        add(textLabel); // 패널에 추가

        // 점수값을 표시하는 라벨 설정
        scoreLabel.setSize(100, 20); // 라벨의 크기 설정
        scoreLabel.setLocation(70, 10); // 라벨의 위치 설정
        add(scoreLabel); // 패널에 추가
    }

    // 점수를 설정하는 메서드
    public void setScore(int value) {
        score = value; // 점수를 전달받은 값으로 설정
    }

    // 점수를 증가시키는 메서드
    public void increase(int value) {
        score += value; // 점수에 전달받은 값을 더함
        scoreLabel.setText(Integer.toString(score)); // 점수 라벨을 갱신
    }

    // 점수를 감소시키는 메서드
    public void decrease(int value) {
        score -= value; // 점수에서 전달받은 값을 뺌
        scoreLabel.setText(Integer.toString(score)); // 점수 라벨을 갱신
    }

    // 현재 점수를 반환하는 메서드
    public int getScore() {
        return score; // 점수값 반환
    }
}
