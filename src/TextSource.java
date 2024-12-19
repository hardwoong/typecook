import java.io.*;
import java.util.Vector;

// TextSource 클래스: 단어를 저장하고 불러오는 기능을 제공
public class TextSource {
    private Vector<String> v = new Vector<>(); // 단어를 저장하는 벡터
    private final String filePath = "words.txt"; // 단어를 저장하는 파일 경로

    // 생성자: 객체 생성 시 단어 파일에서 데이터를 불러옴
    public TextSource() {
        loadWordsFromFile(); // 파일에서 단어 불러오기
    }

    // 단어를 랜덤하게 하나 반환하는 메서드
    public String get() {
        int index = (int) (Math.random() * v.size()); // 랜덤 인덱스 생성
        return v.get(index); // 해당 인덱스의 단어 반환
    }

    // 새로운 단어를 추가하고 파일에 저장하는 메서드
    public void add(String word) {
        v.add(word); // 벡터에 단어 추가
        saveWordToFile(word); // 단어를 파일에 저장
    }

    // 모든 단어를 반환하는 메서드
    public Vector<String> getAllWords() {
        return v; // 저장된 단어 전체 반환
    }

    // 파일에서 단어를 읽어와 벡터에 추가하는 메서드
    private void loadWordsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) { // 파일의 각 줄을 읽음
                v.add(line.trim()); // 읽은 단어를 벡터에 추가 (앞뒤 공백 제거)
            }
        } catch (IOException e) { // 파일 읽기 중 오류 처리
            System.out.println("단어 파일을 읽는 중 오류 발생: " + e.getMessage());
        }
    }

    // 단어를 파일에 저장하는 메서드
    private void saveWordToFile(String word) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(word); // 파일에 단어 쓰기
            writer.newLine(); // 줄바꿈 추가
        } catch (IOException e) { // 파일 저장 중 오류 처리
            System.out.println("단어 파일에 저장하는 중 오류 발생: " + e.getMessage());
        }
    }
}
