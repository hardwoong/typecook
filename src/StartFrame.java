import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

// 프로그램의 메인 시작 화면 구현, 사용자의 정보를 입력 받고 다양한 기능(게임 참가, 단어 편집, 기록 보기) 제공
public class StartFrame extends JFrame {
    // 사용자 이름과 생년월일을 입력받는 텍스트 필드
    private JTextField nameField = new JTextField(15);
    private JTextField birthDateField = new JTextField(15);

    // 난이도를 선택할 수 있는 콤보박스
    private JComboBox<String> difficultyBox = new JComboBox<>(new String[] { "Easy", "Medium", "Hard" });

    // "게임 참가", "단어 편집", "기록 보기" 버튼
    private JButton startButton = new JButton("게임 참가");
    private JButton editWordsButton = new JButton("단어 편집");
    private JButton viewScoresButton = new JButton("기록 보기");

    // 배경음악 실행에 필요한 변수들
    private Clip clip;

    public StartFrame() { // StartFrame 생성자
        // 메인 프레임 초기화
        init();

        // 사용자 입력 및 버튼 UI 설정
        initializeMenu();

        // 버튼에 이벤트 리스너 설정
        initializeBtn();
    }

    private void init() { // 메인 프레임 초기화, 배경 이미지와 창 설정
        playWav("../Start.wav"); // 게임 시작 소리 재생

        setTitle("타자요리사"); // 창 제목 설정
        setSize(700, 600); // 창 크기 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 창 종료 시 프로그램 종료
        setLayout(new BorderLayout()); // 레이아웃 설정

        // 배경 이미지 설정
        JLabel background = new JLabel(new ImageIcon("../startBackgroundImg.png")); // 배경 이미지 설정
        setContentPane(background); // 배경 이미지를 창의 컨텐츠 패인으로 설정
        background.setLayout(new GridBagLayout()); // 배경 레이아웃 설정
    }

    private void initializeMenu() { // 사용자 입력 패널 및 버튼 메뉴 초기화
        JPanel inputPanel = new JPanel(); // 입력 패널
        inputPanel.setLayout(new GridBagLayout());
        inputPanel.setOpaque(false); // 투명하게 설정하여 배경 이미지 보이도록 함
        GridBagConstraints gbc = new GridBagConstraints(); // 레이아웃 제약 조건 설정
        gbc.insets = new Insets(10, 10, 10, 10); // 컴포넌트 간격 설정

        // 난이도 라벨과 콤보박스 추가
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel difficultyLabel = new JLabel("난 이 도:");
        difficultyLabel.setForeground(Color.WHITE);
        inputPanel.add(difficultyLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(difficultyBox, gbc);

        // 이름 라벨과 입력 필드 추가
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel nameLabel = new JLabel("이  름:");
        nameLabel.setForeground(Color.WHITE);
        inputPanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(nameField, gbc);

        // 생년월일 라벨과 입력 필드 추가
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel birthLabel = new JLabel("생년 월일:");
        birthLabel.setForeground(Color.WHITE);
        inputPanel.add(birthLabel, gbc);
        gbc.gridx = 1;
        inputPanel.add(birthDateField, gbc);

        // "게임 참가" 버튼 추가
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(startButton, gbc);

        // "단어 편집" 버튼 추가
        gbc.gridy = 4;
        inputPanel.add(editWordsButton, gbc);

        // "기록 보기" 버튼 추가
        gbc.gridy = 5;
        inputPanel.add(viewScoresButton, gbc);

        // 입력 패널을 프레임에 추가
        add(inputPanel);
        setVisible(true); // 프레임 표시
    }

    private void initializeBtn() { // 버튼 리스너 설정
        // "게임 참가" 버튼 이벤트
        startButton.addActionListener(new StartButtonListener());

        // "단어 편집" 버튼 이벤트
        editWordsButton.addActionListener(new EditWordsButtonListener());

        // "기록 보기" 버튼 이벤트
        viewScoresButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ViewScoresFrame(); // 기록 보기 창 실행
            }
        });
    }

    // "게임 참가" 버튼 리스너 클래스
    private class StartButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 플레이어가 입력한 값으로 변수 초기화 및 저장
            String playerName = nameField.getText();
            String birthDate = birthDateField.getText();
            String difficulty = (String) difficultyBox.getSelectedItem();

            // 이름과 생년월일 입력 확인
            if (playerName.isEmpty() || birthDate.isEmpty()) { // 입력된 정보가 부족하다면 경고 알림
                JOptionPane.showMessageDialog(StartFrame.this, "이름과 생년월일을 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 캐릭터 선택 화면으로 이동
            new SelectCharacter(playerName, birthDate, difficulty);
            dispose(); // 현재 창 닫기
        }
    }

    // "단어 편집" 버튼 리스터 클래스
    private class EditWordsButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new EditWordsFrame(); // 단어 편집 창 실행
        }
    }

    // 게임 시작과 동시에 재생할 사운드
    private void playWav(String musicFilePath) {
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

    // 메인 메소드, 프로그램 시작
    public static void main(String[] args) {
        new StartFrame();
    }
}
