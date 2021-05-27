/**
 * @Author: Kaia
 * @FileName: BoardPlayerBox.java
 */

package object.ui.box;

import component.animation.Animation;
import component.animation.Animator;
import graphics.Sprite;
import object.Player;
import object.GameObject;
import graphics.Text;
import util.FontLibrary;

import java.awt.*;


public class BoardPlayerBox extends GameObject {

    public static final double AppearScale = 1.5;
    public static final double ShowDuration = 0.3;
    public static final double Height=120;

    private GameObject bar;
    private GameObject root;
    private Text text_name;
    private Text text_score;
    private Text text_mistake;
    private Animator amt_scale;
    private Animator amt_alpha;


    public BoardPlayerBox(String id, Player player, double width, double delay, Sprite sprite) {
        super(id,null);
        this.bar=new GameObject(this.id+"_bar", sprite);
        this.bar.resizeTo(width,Height);


        this.root=new GameObject(this.id+"_root",null);
        this.addObject(this.root);

        String score=String.valueOf(player.getScore());
        String mistake=String.valueOf(player.getMistakes());
        this.text_name=new Text(this.id+"name",player.getPlayerName(), FontLibrary.GetPlayerNameFont(30));
        this.text_name.setColor(new Color(212,212,212));
        this.text_name.setPosition(- width * 0.2,Height*0.2);
        this.text_score=new Text(this.id+"score","Score: " + score,FontLibrary.GetPlayerNameFont(30));
        this.text_score.setColor(new Color(212,212,212));
        this.text_score.setPosition(width * 0.05,Height * 0.2);
        this.text_mistake=new Text(this.id+"mistake","Mistake: " + mistake, FontLibrary.GetPlayerNameFont(30));
        this.text_mistake.setColor(new Color(212,212,212));
        this.text_mistake.setPosition(width* 0.30,  Height * 0.2);
        this.root.addObject(this.bar);
        this.root.addObject(this.text_name);
        this.root.addObject(this.text_score);
        this.root.addObject(this.text_mistake);


        this.root.setScale(1.5);
        this.root.setAlpha(0);
        this.amt_scale =new Animator(1.5);
        this.amt_alpha =new Animator(0);
        this.addComponent(this.amt_scale);
        this.addComponent(this.amt_alpha);
        this.amt_scale.append(Animation.GetTanh(AppearScale,1,ShowDuration,true,delay));
        this.amt_alpha.append(Animation.GetTanh(0,1,ShowDuration,true,delay));
    }

    public void update(double dt){
        super.update(dt);

        this.root.setAlpha(this.amt_alpha.val());
        this.root.setScale(this.amt_scale.val());
    }

}