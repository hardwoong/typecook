import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// 단어 목록을 편집하고 추가할 수 있는 GUI 프레임 클래스
public class EditWordsFrame extends JFrame {
    private TextSource textSource = new TextSource(); // 단어 저장소 객체
    private JTextArea wordsArea = new JTextArea(10, 30); // 현재 단어 목록을 표시할 텍스트 영역
    private JTextField newWordField = new JTextField(20); // 새 단어를 입력받는 텍스트 필드
    private JButton addWordButton = new JButton("추가"); // 단어 추가 버튼

    // 생성자: 프레임 설정 및 GUI 초기화
    public EditWordsFrame() {
        setTitle("단어 편집"); // 창 제목 설정
        setSize(400, 400); // 창 크기 설정
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 창 닫아도 메인 프로그램은 종료되지 않음
        setLayout(new BorderLayout()); // 레이아웃 매니저를 BorderLayout으로 설정

        // 단어 목록 표시 패널 설정
        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new BorderLayout()); // BorderLayout 사용
        wordsArea.setEditable(false); // 단어 목록은 읽기 전용
        displayPanel.add(new JScrollPane(wordsArea), BorderLayout.CENTER); // 스크롤 가능한 영역 추가
        updateWordsDisplay(); // 초기 단어 목록 표시

        // 새 단어 추가 패널 설정
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout()); // FlowLayout 사용: 컴포넌트를 나란히 배치
        inputPanel.add(new JLabel("새 단어:")); // 새 단어 입력 라벨 추가
        inputPanel.add(newWordField); // 새 단어 입력 필드 추가
        inputPanel.add(addWordButton); // 추가 버튼 추가

        // 각 패널을 프레임에 배치
        add(displayPanel, BorderLayout.CENTER); // 단어 목록 표시 영역
        add(inputPanel, BorderLayout.SOUTH);    // 새 단어 입력 영역

        // 추가 버튼에 이벤트 리스너 설정
        addWordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newWord = newWordField.getText().trim(); // 입력된 단어 가져오기 (앞뒤 공백 제거)
                if (!newWord.isEmpty()) { // 입력이 비어있지 않을 경우
                    textSource.add(newWord); // 단어 저장소에 추가
                    updateWordsDisplay(); // 단어 목록 갱신
                    newWordField.setText(""); // 입력 필드 초기화
                    // 성공 메시지 표시
                    JOptionPane.showMessageDialog(EditWordsFrame.this, "단어가 추가되었습니다!", "알림", JOptionPane.INFORMATION_MESSAGE);
                } else { // 입력이 비어있을 경우 경고 메시지 표시
                    JOptionPane.showMessageDialog(EditWordsFrame.this, "단어를 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        setVisible(true); // 프레임 표시
    }

    // 단어 목록을 갱신하여 텍스트 영역에 표시하는 메서드
    private void updateWordsDisplay() {
        wordsArea.setText(""); // 기존 텍스트 삭제
        for (String word : textSource.getAllWords()) { // 저장된 모든 단어 가져오기
            wordsArea.append(word + "\n"); // 각 단어를 한 줄씩 텍스트 영역에 추가
        }
    }
}
