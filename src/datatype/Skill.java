/**
 * @Author: RogerDTZ
 * @FileName: Skill.java
 */

package datatype;

public enum Skill {

    None(1),
    GodPick(2),
    Rude(3);

    public int cost;


    Skill(int cost) {
        this.cost = cost;
    }

    public static Skill Get(int i) {
        if (i == 0)
            return Skill.None;
        if (i == 1)
            return Skill.GodPick;
        return Skill.Rude;
    }

    public static Skill Get(String name) {
        if (name.equals("None"))
            return Skill.None;
        if (name.equals("GodPick"))
            return Skill.GodPick;
        return Skill.Rude;
    }

}
