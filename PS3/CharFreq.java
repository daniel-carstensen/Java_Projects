public class CharFreq {

    char character;
    int frequency;

    public CharFreq(char character, int frequency) {
        this.character = character;
        this.frequency = frequency;
    }

    public char getCharacter() {
        return character;
    }

    public int getFrequency() {
        return frequency;
    }

    @Override
    public String toString() {
        return "CharacterAndFrequency{" +
                "character=" + character +
                ", frequency=" + frequency +
                '}';
    }
}
