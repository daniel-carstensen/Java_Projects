public class CharacterAndFrequency {

    char character;
    int frequency;

    public CharacterAndFrequency (char character, int frequency) {
        this.character = character;
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "CharacterAndFrequency{" +
                "character=" + character +
                ", frequency=" + frequency +
                '}';
    }
}
