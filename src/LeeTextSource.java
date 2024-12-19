import java.io.*;
import java.util.Vector;

// LeeTextSource 클래스: TextSource를 확장하여 "lee_words.txt" 전용 단어 관리
public class LeeTextSource extends TextSource {
    private Vector<String> v = new Vector<>(); // 단어를 저장하는 벡터
    private final String filePath = "lee_words.txt"; // LeeTextSource 전용 단어 파일 경로

    // 생성자: 객체 생성 시 파일에서 단어를 불러옴
    public LeeTextSource() {
        loadWordsFromFile(); // 단어 파일 불러오기
    }

    // 단어를 랜덤하게 하나 반환하는 메서드
    public String get() {
        int index = (int) (Math.random() * v.size()); // 랜덤 인덱스 생성
        return v.get(index); // 해당 인덱스의 단어 반환
    }

    // 새로운 단어를 추가하고 파일에 저장하는 메서드
    public void add(String word) {
        v.add(word); // 벡터에 단어 추가
        saveWordToFile(word); // 파일에 단어 저장
    }

    // 모든 단어를 반환하는 메서드
    public Vector<String> getAllWords() {
        return v; // 현재 저장된 단어 리스트 반환
    }

    // 파일에서 단어를 읽어 벡터에 추가하는 메서드
    private void loadWordsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) { // 파일의 각 줄을 읽음
                v.add(line.trim()); // 읽은 단어를 벡터에 추가 (앞뒤 공백 제거)
            }
        } catch (FileNotFoundException e) { 
            // 파일이 존재하지 않을 경우: 새 파일이 생성될 것이라는 메시지 출력
            System.out.println("단어 파일이 존재하지 않습니다. 새로 생성됩니다: " + filePath);
        } catch (IOException e) {
            // 파일 읽기 중 발생한 오류 처리
            System.out.println("단어 파일을 읽는 중 오류 발생: " + e.getMessage());
        }
    }

    // 단어를 파일에 저장하는 메서드
    private void saveWordToFile(String word) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(word); // 단어를 파일에 쓰기
            writer.newLine(); // 줄바꿈 추가
        } catch (IOException e) {
            // 파일 쓰기 중 발생한 오류 처리
            System.out.println("단어 파일에 저장하는 중 오류 발생: " + e.getMessage());
        }
    }
}
