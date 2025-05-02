package CRDT;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class ID {

    public String user;
    public long timeStamp;

    public ID(String user, long timeStamp) {
        this.user = user;
        this.timeStamp = timeStamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true; // same object

                }if (obj == null || getClass() != obj.getClass()) {
            return false; // null or different class

                }ID other = (ID) obj;
        return timeStamp == other.timeStamp && Objects.equals(user, other.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, timeStamp); // generates a consistent hash
    }

    @Override
    public String toString() {
        return "(" + user + ", " + timeStamp + ")";
    }

}
