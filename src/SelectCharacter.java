import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// 플레이어로부터 캐릭터를 선택하도록 하는 창 구현
public class SelectCharacter extends JFrame {
    private String playerName; // 사용자 이름
    private String birthDate; // 사용자 생년월일
    private String difficulty; // 난이도

    JPanel characterPanel;
    JPanel captionPanel;

    // SelectCharacter 생성자 (플레이어 이름, 생년월일, 난이도 넘겨 받음)
    public SelectCharacter(String playerName, String birthDate, String difficulty) {
        // 플레이어가 입력한 값으로 변수 초기화 및 저장
        this.playerName = playerName;
        this.birthDate = birthDate;
        this.difficulty = difficulty;

        // 프레임, 배경 설정
        init();

        // 캐릭터 사진 패널 설정
        initCharPanel();

        // 캡션 패널 설정
        initCapPanel();

        // 구성된 패널을 프레임에 추가
        addPan();
    }

    private void init() {
        setTitle("캐릭터 선택"); // 창 제목 설정
        setSize(700, 600); // 창 크기 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 창 닫기 버튼 동작 설정
        setLayout(new BorderLayout()); // 레이아웃 설정

        // 배경 이미지 설정
        JLabel background = new JLabel(new ImageIcon("../selectCharacterBackground.png")); // 배경 이미지 설정
        setContentPane(background); // 배경 이미지를 프레임의 ContentPane으로 설정
        background.setLayout(new BorderLayout()); // 레이아웃 설정
        setVisible(true); // 프레임 표시
    }

    private void initCharPanel() {
        // 캐릭터 선택 패널 설정 (1행 4열의 그리드 레이아웃)
        characterPanel = new JPanel();
        characterPanel.setLayout(new GridLayout(1, 4, 10, 10)); // 간격 설정 (가로 10, 세로 10)
        characterPanel.setOpaque(false); // 배경 투명 설정
        characterPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // 여백 설정

        // 각 캐릭터 버튼을 생성하고 패널에 추가
        addCharacterButton(characterPanel, "나폴리맛피아", "../Matpia/Matpia.png", "MatpiaGame");
        addCharacterButton(characterPanel, "평가절하", "../Pyung/Pyung.png", "PyungGame");
        addCharacterButton(characterPanel, "백종원", "../Baek/Baek.png", "BaekGame");
        addCharacterButton(characterPanel, "에드워드 리", "../Lee/Lee.png", "LeeGame");
    }

    private void initCapPanel() {
        // 설명 패널 설정 (각 캐릭터 이름을 표시)
        captionPanel = new JPanel();
        captionPanel.setLayout(new GridLayout(1, 4, 10, 10)); // 1행 4열의 레이아웃
        captionPanel.setOpaque(false); // 배경 투명 설정
        captionPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20)); // 여백 설정

        // 각 캐릭터의 설명(이름)을 생성하고 패널에 추가
        captionPanel.add(createCaptionLabel("나폴리맛피아"));
        captionPanel.add(createCaptionLabel("평가절하"));
        captionPanel.add(createCaptionLabel("백종원"));
        captionPanel.add(createCaptionLabel("에드워드 리"));
    }

    private void addPan() {
        add(characterPanel, BorderLayout.CENTER); // 캐릭터 버튼 패널을 중앙에 추가
        add(captionPanel, BorderLayout.SOUTH); // 설명 패널을 하단에 추가
    }

    private JLabel createCaptionLabel(String text) { // 캡션 라벨 생성 함수
        JLabel label = new JLabel(text, SwingConstants.CENTER); // 가운데 정렬된 텍스트 라벨 생성
        label.setForeground(Color.WHITE); // 글자색을 흰색으로 설정
        label.setFont(new Font("Arial", Font.BOLD, 14)); // 폰트 설정
        return label; // 생성된 라벨 반환
    }

    // 캐릭터 버튼 생성하고 이벤트 설정하는 함수
    private void addCharacterButton(JPanel panel, String characterName, String imagePath, String gameClassName) {
        // 캐릭터 버튼 생성
        JButton characterButton = new JButton(new ImageIcon(imagePath)); // 이미지 설정
        characterButton.setPreferredSize(new Dimension(150, 200)); // 버튼 크기 설정
        characterButton.setBorder(BorderFactory.createEmptyBorder()); // 버튼 테두리 제거
        characterButton.setContentAreaFilled(false); // 버튼 배경 제거 (투명화)

        // 버튼 클릭 이벤트 설정
        characterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // 선택된 캐릭터에 맞는 게임 클래스를 동적으로 로드
                    Class<?> gameClass = Class.forName(gameClassName); // 클래스 이름으로 클래스 찾기
                    JFrame gameFrame = (JFrame) gameClass
                        .getDeclaredConstructor(String.class, String.class, String.class) // 생성자 찾기
                        .newInstance(playerName, birthDate, difficulty); // 인스턴스 생성
                    gameFrame.setVisible(true); // 새 게임 창 보이기
                    dispose(); // 현재 캐릭터 선택 창 닫기
                } catch (Exception ex) {
                    ex.printStackTrace(); // 예외 발생 시 출력
                }
            }
        });

        panel.add(characterButton); // 버튼을 패널에 추가
    }
}
