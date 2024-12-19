import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ViewScoresFrame extends JFrame {
    private static final String FILE_PATH = "game_results.txt"; // 게임 결과 파일 경로
    private JTextArea rankingArea; // 랭킹 표시 영역

    public ViewScoresFrame() {
        setTitle("기록 보기");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 창 닫아도 프로그램 종료되지 않음
        setLayout(new BorderLayout());

        // 상단 제목
        JLabel titleLabel = new JLabel("랭킹 (최단 시간 순)");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // 랭킹 표시 영역
        rankingArea = new JTextArea();
        rankingArea.setEditable(false); // 편집 불가
        JScrollPane scrollPane = new JScrollPane(rankingArea);
        add(scrollPane, BorderLayout.CENTER);

        // 랭킹 로드
        loadAndDisplayScores();

        setVisible(true);
    }

    private void loadAndDisplayScores() {
        List<ScoreRecord> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(", "); // 파일 내용은 쉼표로 구분
                if (parts.length == 3) {
                    String name = parts[0];
                    String birthDate = parts[1];
                    String timeStr = parts[2].trim();

                    double elapsedTime = Double.parseDouble(timeStr); // 숫자 파싱
                    records.add(new ScoreRecord(name, birthDate, elapsedTime));
                }
            }

            // 시간 기준으로 정렬 (오름차순)
            records.sort(Comparator.comparingDouble(ScoreRecord::getTime));

            // 상위 10개 출력
            rankingArea.setText(""); // 기존 텍스트 초기화
            rankingArea.append("순위\t이름\t생년월일\t시간\n");
            rankingArea.append("====================================\n");

            for (int i = 0; i < Math.min(records.size(), 10); i++) {
                ScoreRecord record = records.get(i);
                rankingArea.append((i + 1) + "\t" + record.getName() + "\t" +
                        record.getBirthDate() + "\t" +
                        String.format("%.2f초", record.getTime()) + "\n");
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "기록 파일을 불러오는 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 기록을 저장하는 ScoreRecord 클래스
    private static class ScoreRecord {
        private final String name;
        private final String birthDate;
        private final double time;

        public ScoreRecord(String name, String birthDate, double time) {
            this.name = name;
            this.birthDate = birthDate;
            this.time = time;
        }

        public String getName() {
            return name;
        }

        public String getBirthDate() {
            return birthDate;
        }

        public double getTime() {
            return time;
        }
    }
}
