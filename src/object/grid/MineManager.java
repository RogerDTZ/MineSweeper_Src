/**
 * @Author: RogerDTZ
 * @FileName: MineManager.java
 */

package object.grid;

import component.animation.Animation;
import datatype.*;
import graphics.Sprite;
import javafx.util.Pair;
import main.AudioManager;
import object.Player;
import object.GameObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import util.Random;

public class MineManager extends GameObject {

    public final static double MineAnimationDeltaTime = 0.1;

    public final static double ZoomDuration = 0.2;
    public final static double ZoomMaxSize = 30;
    public static final int ShockingRadius = 10;
    public static final double ShockingTime = 1;

    private Vector2 gridSize;
    private double mineSize;
    private int gridWidth;
    private int gridHeight;
    private int mineNum;
    private Mine[][] mines;
    private GameObject mineContainer;
    private boolean hasInitMines;

    private Queue<Operation> queue;

    private Sprite mineBackground;

    private FlameManager explosion;
    private CheerPlayer cheerPlayer;

    private boolean touchable;

    private Skill currSkill;

    private int ptr_x, ptr_y;
    private boolean isCurrentLocal;
    private int remote_x, remote_y;
    private Skill remote_skill;


    public MineManager(Vector2 size, int width, int height, Sprite mineBackground, Vector2 imgOffset) {
        super("mine_manager");
        this.gridSize = size;
        this.gridWidth = width;
        this.gridHeight = height;
        this.mineSize = this.gridSize.x / this.gridWidth;
        this.mines = new Mine[width][height];
        this.mineContainer = new GameObject("mine_container");
        this.mineContainer.setRenderPriority(-1);
        this.addObject(this.mineContainer);

        this.mineBackground = mineBackground;

        this.queue = new LinkedList<>();

        for (int j = 0; j < gridHeight; ++j) {
            for (int i = 0; i < gridWidth; ++i) {
                mines[i][j] = new Mine(
                        -this.gridSize.x / 2 + mineSize * i + 0.5 * mineSize,
                        -this.gridSize.y / 2 + mineSize * j + 0.5 * mineSize,
                        mineSize * 0.90,
                        i, j,
                        this.mineBackground, imgOffset,
                        1 + Random.nextDouble(0, 1.0));
                this.mineContainer.addObject(mines[i][j]);
            }
        }
        this.hasInitMines = false;

        this.explosion = new FlameManager(this.id + "_flame");
        this.addObject(this.explosion);

        this.cheerPlayer = new CheerPlayer(this.id + "_cheerPlayer");
        this.addObject(this.cheerPlayer);

        this.touchable = true;

        this.ptr_x = this.ptr_y = -1;
        this.currSkill = Skill.None;
        this.remote_x = this.remote_y = -1;
        this.remote_skill = Skill.None;
    }

    public void setCheat(boolean flag) {
        for (int i = 0; i < gridWidth; ++i)
            for (int j = 0; j < gridHeight; ++j)
                this.mines[i][j].toggleCheating(flag);
    }

    public int getInitState() {
        return this.hasInitMines ? 2 : 1;
    }

    // [Server]
    public String server_generate(int mineNum, int sx, int sy) {
        boolean[][] isMine = new boolean[gridWidth][gridHeight];
        for (int i = 0; i < mineNum; i++) {
            int x, y;
            while (true) {
                x = Random.nextInt(gridWidth);
                y = Random.nextInt(gridHeight);
                if (isMine[x][y])
                    continue;
                isMine[x][y] = true;
                if (this.server_checkLegal(x, y, isMine) && !(x == sx && y == sy))
                    break;
                isMine[x][y] = false;
            }
        }
        StringBuilder res = new StringBuilder();
        for (int j = 0; j < gridHeight; ++j)
            for (int i = 0; i < gridWidth; ++i)
                res.append(isMine[i][j] ? '2' : '0');
        return res.toString();
    }

    public String exportMap(boolean intoFile) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < this.gridHeight; ++j) {
            for (int i = 0; i < this.gridWidth; ++i) {
                Mine x = this.mines[i][j];
                if (x.isMine()) {
                    if (x.hasFlag())
                        sb.append('3');
                    else if (x.isRevealed())
                        sb.append('4');
                    else
                        sb.append('2');
                } else {
                    sb.append(!x.isRevealed() ? '0' : '1');
                }
            }
            if (intoFile)
                sb.append('\n');
        }
        return sb.toString();
    }

    // [Server]
    private boolean server_checkLegal(int x, int y, boolean[][] isMine) {
        int[] dx = {-1, -1, -1, 0, 0, 0, +1, +1, +1};
        int[] dy = {-1, 0, +1, -1, 0, +1, -1, 0, +1};
        for (int k = 0; k < 9; ++k) {
            int sx = x + dx[k], sy = y + dy[k];
            if (!this.server_checkNo9(sx, sy, isMine))
                return false;
        }
        return true;
    }

    // [Server]
    private boolean server_checkNo9(int x, int y, boolean[][] isMine) {
        int[] dx = {-1, -1, -1, 0, 0, 0, +1, +1, +1};
        int[] dy = {-1, 0, +1, -1, 0, +1, -1, 0, +1};
        int cnt = 0;
        for (int k = 0; k < 9; ++k) {
            int sx = x + dx[k], sy = y + dy[k];
            if (0 <= sx && sx < this.gridWidth && 0 <= sy && sy < this.gridHeight && isMine[sx][sy])
                ++cnt;
        }
        return cnt < 9;
    }

    public void serverCallback_initMines(String data) {
        /*
        if (this.hasInitMines)
            return;
         */
        this.hasInitMines = true;
        this.mineNum = 0;
        for (int j = 0; j < gridHeight; ++j) {
            for (int i = 0; i < gridWidth; ++i) {
                int cnt = 0;
                int[] dx = {-1, -1, -1, 0, 0, +1, +1, +1};
                int[] dy = {-1, 0, +1, -1, +1, -1, 0, +1};
                for (int k = 0; k < 8; ++k) {
                    int sx = i + dx[k], sy = j + dy[k];
                    if (0 <= sx && sx < gridWidth && 0 <= sy && sy < gridHeight) {
                        char nc = data.charAt(sy * gridWidth + sx);
                        if (nc == '2' || nc == '3' || nc == '4')
                            ++cnt;
                    }
                }
                char c = data.charAt(j * gridWidth + i);
                if (c == '0' || c == '1') {
                    mines[i][j].setMine(cnt + ((c == '1') ? 10 : 0));
                } else {
                    ++this.mineNum;
                    if (c == '2') // mine, not seen
                        mines[i][j].setMine(-1);
                    else if (c == '3') // mine, flag
                        mines[i][j].setMine(-2);
                    else // mine, exploded
                        mines[i][j].setMine(-3);
                }
            }
        }
    }

    public void serverCallback_resetMines() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.gridHeight * this.gridWidth; ++i)
            sb.append('0');
        this.serverCallback_initMines(sb.toString());
        this.hasInitMines = false;
    }

    public void serverCallback_coverMines() {
        for (int i = 0; i < this.gridWidth; ++i)
            for (int j = 0; j < this.gridHeight; ++j)
                this.mines[i][j].cover();
    }

    // [Client]
    @Override
    public void update(double dt) {
        super.update(dt);

        this.ptr_x = -1;
        this.ptr_y = -1;

        for (int i = 0; i < gridWidth; ++i) {
            for (int j = 0; j < gridHeight; ++j) {
                this.mines[i][j].update(dt);
                int event = this.mines[i][j].isClicked();
                if (event != 0 && this.touchable) {
                    if (event == 1) {
                        if (this.currSkill == Skill.None)
                            this.queue.offer(new Operation(-1, i, j, Operation.OperationType.Normal));
                        else if (this.currSkill == Skill.GodPick)
                            this.queue.offer(new Operation(-1, i, j, Operation.OperationType.GodPick));
                        else if (this.currSkill == Skill.Rude)
                            this.queue.offer(new Operation(-1, i, j, Operation.OperationType.Rude));
                    } else if (event == 2) {
                        //this.queue.offer(new Operation(i, j, Operation.OperationType.Normal));
                    } else if (event == 3) {
                        this.queue.offer(new Operation(-1, i, j, Operation.OperationType.Mark));
                    }
                }
                if (this.mines[i][j].isHovering()) {
                    this.ptr_x = i;
                    this.ptr_y = j;
                }
            }
        }

        int x = ptr_x, y = ptr_y;
        Skill skill = currSkill;
        if (!this.isCurrentLocal) {
            x = this.remote_x;
            y = this.remote_y;
            skill = this.remote_skill;
        }
        for (int i = 0; i < this.gridWidth; ++i)
            for (int j = 0; j < this.gridHeight; ++j)
                this.mines[i][j].toggleAim(false, 0);
        if (x != -1) {
            if (skill == Skill.None) {
                this.mines[x][y].toggleAim(true, 0);
            } else if (skill == Skill.Rude) {
                for (int dx = -1; dx <= +1; ++dx) {
                    for (int dy = -1; dy <= +1; ++dy) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (0 <= nx && nx < this.gridWidth && 0 <= ny && ny < this.gridHeight) {
                            this.mines[nx][ny].toggleAim(true, 0);
                        }
                    }
                }
            } else if (skill == Skill.GodPick) {
                this.mines[x][y].toggleAim(true, 1);
            }
        }
    }

    // [Client]
    public Operation client_getOperation() {
        return this.queue.poll();
    }

    // [Server]
    public ArrayList<OperationResult> server_getOperationResult(Player player, Operation op) {
        ArrayList<OperationResult> res = new ArrayList<>();
        if (!this.mines[op.x][op.y].isRevealed()) {
            if (op.type == Operation.OperationType.Normal) {
                if (this.mines[op.x][op.y].isMine()) {
                    if (!this.mines[op.x][op.y].hasFlag())
                        res.add(new OperationResult(new AnimationData(op.x, op.y, 0, "reveal"), new ScoreData(player.getPlayerID(), -1, 0, 1)));
                } else {
                    Queue<Pair<Pair<Integer, Integer>, Double>> q = new LinkedList<>();
                    q.offer(new Pair<>(new Pair<>(op.x, op.y), 0.0));
                    boolean[][] visit = new boolean[gridWidth][gridHeight];
                    visit[op.x][op.y] = true;
                    int[] dx = {-1, -1, -1, 0, 0, +1, +1, +1};
                    int[] dy = {-1, 0, +1, -1, +1, -1, 0, +1};
                    while (!q.isEmpty()) {
                        int x = q.peek().getKey().getKey();
                        int y = q.peek().getKey().getValue();
                        double t = q.peek().getValue();
                        q.poll();
                        res.add(new OperationResult(new AnimationData(x, y, t, "reveal"), new ScoreData(player.getPlayerID(), 0, 0, (x == op.x && y == op.y) ? 1 : 0)));
                        if (this.mines[x][y].getNeighbourCnt() == 0) {
                            for (int k = 0; k < 8; ++k) {
                                int nx = x + dx[k], ny = y + dy[k];
                                if (0 <= nx && nx < gridWidth && 0 <= ny && ny < gridHeight && !visit[nx][ny] && !this.mines[nx][ny].isRevealed()) {
                                    q.offer(new Pair<>(new Pair<>(nx, ny), t + MineAnimationDeltaTime));
                                    visit[nx][ny] = true;
                                }
                            }
                        }
                    }
                }
            } else if (op.type == Operation.OperationType.Mark) {
                if (this.mines[op.x][op.y].isMine()) {
                    if (!this.mines[op.x][op.y].hasFlag())
                        res.add(new OperationResult(new AnimationData(op.x, op.y, 0, "mark"), new ScoreData(player.getPlayerID(), +1, 0, 1)));
                } else {
                    res.add(new OperationResult(new AnimationData(op.x, op.y, 0, "mark"), new ScoreData(player.getPlayerID(), 0, +1, 1)));
                }
            } else if (op.type == Operation.OperationType.GodPick) {
                if (!this.mines[op.x][op.y].isRevealed() && !this.mines[op.x][op.y].hasFlag()) {
                    if (this.mines[op.x][op.y].isMine()) {
                        res.add(new OperationResult(new AnimationData(op.x, op.y, 0, "mark"), new ScoreData(player.getPlayerID(), +1, 0, Skill.GodPick.cost)));
                    } else {
                        res.add(new OperationResult(new AnimationData(op.x, op.y, 0, "reveal"), new ScoreData(player.getPlayerID(), 0, 0, Skill.GodPick.cost)));
                    }
                }
            } else if (op.type == Operation.OperationType.Rude) {
                int[] dx = {-1, -1, -1, 0, 0, 0, +1, +1, +1};
                int[] dy = {-1, 0, +1, -1, 0, +1, -1, 0, +1};
                int cost = Skill.Rude.cost;
                for (int k = 0; k < 9; ++k) {
                    int nx = op.x + dx[k];
                    int ny = op.y + dy[k];
                    if (0 <= nx && nx < this.gridWidth && 0 <= ny && ny < this.gridHeight) {
                        res.add(new OperationResult(new AnimationData(nx, ny, 0, "reveal"),
                                new ScoreData(player.getPlayerID(), this.mines[nx][ny].isMine() ? -1 : 0, 0, cost)));
                        cost = 0;
                    }
                }
            }
        }
        res.sort(Comparator.comparingDouble(o -> o.animation.delay));
        return res;
    }

    public int server_getRemainingMineNum() {
        if (!this.hasInitMines)
            return -1; // unexpected
        int res = 0;
        for (int i = 0; i < this.gridWidth; ++i)
            for (int j = 0; j < this.gridHeight; ++j) {
                if (this.mines[i][j].isMine() && !this.mines[i][j].isRevealed() && !this.mines[i][j].hasFlag())
                    ++res;
            }
        return res;
    }

    // [Client]
    public void client_playAnimation(AnimationData data) {
        switch(data.info) {
            case "reveal":
                this.mines[data.x][data.y].reveal(data.delay);
                if (data.delay == 0)
                    AudioManager.PlayWithVolume("wood", 1.0, 0.0);
                if (this.mines[data.x][data.y].isMine()) {
                    this.explosion.spawn(this.mines[data.x][data.y].getTransform().position, data.delay + ZoomDuration + 0.3);
                    this.setShockingWave(data.x, data.y, data.delay + ZoomDuration + 0.6);
                    /*
                    this.toggleZoom(true, data.x, data.y, data.delay);
                    this.toggleZoom(false, data.x, data.y, 0.6);
                     */
                }
                break;
            case "mark":
                this.mines[data.x][data.y].mark(data.delay);
                if (this.mines[data.x][data.y].isMine()) {
                    this.cheerPlayer.play(this.mines[data.x][data.y].getTransform().position, data.delay + Mine.FlagShowDuration - 0.1);
                } else {
                    this.mines[data.x][data.y].reveal(data.delay + Mine.FlagShowDuration + 0.5);
                }
                break;
        }
    }

    public boolean hasInitMines() {
        return this.hasInitMines;
    }

    private void setShockingWave(int px, int py, double delay) {
        for (int dx = -ShockingRadius; dx <= +ShockingRadius; ++ dx) {
            for (int dy = -ShockingRadius; dy <= +ShockingRadius; ++ dy) {
                int x = px + dx;
                int y = py + dy;
                double dis = Math.sqrt(1.0 * dx * dx + 1.0 * dy * dy);
                if (0 <= x && x < this.gridWidth && 0 <= y && y < this.gridHeight && dis <= ShockingRadius) {
                    this.mines[x][y].jump(this.mineSize * 0.4 * (1 - dis / ShockingRadius), delay + ShockingTime * dis / ShockingRadius);
                }
            }
        }
    }

    public void setTouchable(boolean flag) {
        this.touchable = flag;
    }

    public void setCurrSkill(Skill skill) {
        this.currSkill = skill;
    }

    public Pair<Integer, Integer> getCursor() {
        return new Pair<>(this.ptr_x, this.ptr_y);
    }

    public void serverCallback_setRemoteCursor(int x, int y, Skill skill) {
        this.remote_x = x;
        this.remote_y = y;
        this.remote_skill = skill;
    }

    public void setIsCurrentLocal(boolean flag) {
        this.isCurrentLocal = flag;
    }

}

