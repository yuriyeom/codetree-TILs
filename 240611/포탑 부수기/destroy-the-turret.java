import java.util.*;
import java.io.*;
public class Main {
    static int N, M, K;
    static List<Turret> list;
    static boolean[][] isBroken;
    static int[] dx = {0, 1, 0, -1}; // 우하좌상
    static int[] dy = {1, 0, -1, 0};
    public static void main(String[] args) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        StringTokenizer st = new StringTokenizer(br.readLine());
        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());
        K = Integer.parseInt(st.nextToken());

        list = new ArrayList<>();
        isBroken = new boolean[N+1][M+1];

        boolean[][] visited = new boolean[N+1][M+1];

        // 입력
        for(int i=1; i<=N; i++){
            st = new StringTokenizer(br.readLine());
            for(int j=1; j<=M; j++){
                int item = Integer.parseInt(st.nextToken());
                if(item == 0) {
                    isBroken[i][j] = true;
                }else{
                    list.add(new Turret(i, j, 0, item));
                }
            }
        }
    
        // K번 반복. 포탑이 하나도 부서지지 않으면 종료
        int time = 1;
        while(K-- > 0){
            visited = new boolean[N+1][M+1];
            System.out.println("========" + K);
            // for(Turret turret : list){
            //     System.out.println(turret);
            // }
            // 1. 공격자 선정
            Collections.sort(list, new Comparator<Turret>(){
                public int compare(Turret a, Turret b){
                    if(a.power == b.power){
                        if(a.time == b.time){
                            if(a.x + a.y == b.x + b.y){
                                return -Integer.compare(a.y, b.y); // 가장 큰
                            }
                            return -Integer.compare(a.x + a.y, b.x + b.y); // 가장 큰
                        }
                        return -Integer.compare(a.time, b.time); // 가장 큰
                    }
                    return Integer.compare(a.power, b.power); // 가장 작은
                }
                
            });

            Turret t = list.get(0);
            //  System.out.println("공격자 " + t);
            t.power += N + M;
            t.time = time++;
            visited[t.x][t.y] = true;
            System.out.println("공격자 " + t);

            // 2. 공격자의 공격
            // 가장 강한 포탑 선정
            Collections.sort(list, new Comparator<Turret>(){
                public int compare(Turret a, Turret b){
                    if(a.power == b.power){
                        if(a.time == b.time){
                            if(a.x + a.y == b.x + b.y){
                                return Integer.compare(a.y, b.y); // 가장 작은
                            }
                            return Integer.compare(a.x + a.y, b.x + b.y); // 가장 작은
                        }
                        return Integer.compare(a.time, b.time); // 가장 작은
                    }
                    return -Integer.compare(a.power, b.power); // 가장 큰
                }
            });

            Turret target = list.get(0);
            for(int i=1; i<list.size(); i++){
                if(t.x != target.x || t.y != target.y) break;
                target = list.get(i);
            }
            
            visited[target.x][target.y] = true;
            System.out.println("가장 강한 포탑 " + target);

            // 레이저 공격
            ArrayList<int[]> result = bfs(t.x, t.y, target.x, target.y);
            // 최단경로가 있으면
            if(result != null){
                System.out.println("레이저 공격");
                target.power -= t.power;
                // 경로에 있는 포탑에도 t.power/2만큼 피해
                for(int[] r : result){
                    for(Turret turret : list){
                        if(r[0] == turret.x && r[1] == turret.y){
                            turret.power -= t.power / 2;
                            visited[r[0]][r[1]] = true;
                            break;
                        }
                    }
                }
            }
            
            // 최단경로가 없으면 포탄 공격
            else{
                System.out.println("포탄 공격");
                target.power -= t.power;
                int[] ddx = {0, 0, 1, 1, 1, -1, -1, -1};
                int[] ddy = {1, -1, -1, 0, 1, -1, 0, 1};

                for(int i=0; i<8; i++){
                    int nx = target.x + ddx[i];
                    int ny = target.y + ddy[i];

                    if(nx <= 0) nx = N;
                    else if(nx > N) nx = 1;
                    if(ny <= 0) ny = M;
                    else if(ny > M) ny = 1;

                    if(nx == t.x && ny == t.y) continue;

                    for(Turret turret : list){
                        if(turret.x == nx && turret.y == ny){
                            turret.power -= (t.power/2);
                            visited[turret.x][turret.y] = true;
                        }
                    }
                }
            }

            // for(Turret turret : list)
            //     System.out.println(turret);

            // 3. 포탑 부서짐
            for(Turret turret : list){
                if(turret.power <= 0){
                    isBroken[turret.x][turret.y] = true;
                }
            }

            // 4. 포탑 정비
            for(Turret turret : list){
                if(visited[turret.x][turret.y]) continue;
                turret.power++;
            }
            
            ArrayList<Turret> newList = new ArrayList<>();

            for(Turret turret : list){
                if(turret.power > 0) newList.add(turret);
                // System.out.println(turret);
            }

            list = newList;

            if(list.size() == 1) break;

            int[][] map = new int[N+1][M+1];
            for(Turret turret : list){
                map[turret.x][turret.y] = turret.power;
            }

            for(int i=1; i<=N; i++){
                for(int j=1; j<=M; j++){
                    System.out.print(map[i][j] + " ");
                }
                System.out.println();
            } 
        }
   
    
        int max = 0;
        for(Turret t : list)
            max = Math.max(max, t.power);

        System.out.println(max);

    }

    public static ArrayList<int[]> bfs(int sx, int sy, int ex, int ey){
        Queue<Point> queue = new LinkedList<>();
        boolean[][] visited = new boolean[N+1][M+1];
        int[][] route = new int[N+1][M+1];

        queue.offer(new Point(sx, sy));
        visited[sx][sy] = true;
        route[sx][sy] = 1;

        while(!queue.isEmpty()){
            Point p = queue.poll();

            if(p.x == ex && p.y == ey){
                // System.out.println("도착");
                ArrayList<int[]> routeList = new ArrayList<>();

                int[] rdx = {-1, 0, 1, 0};
                int[] rdy = {0, -1, 0, 1};
                int item = route[ex][ey] - 1;
                int x = ex;
                int y = ey;
                // for(int i=1; i<=N; i++){
                //     for(int j=1; j<=M; j++){
                //         System.out.print(route[i][j] + " ");
                //     }
                //     System.out.println();
                // }
                while(true){
                    // System.out.println(x + " " + y + " " + item);
                    if(x == sx && y == sy) return routeList;
                    for(int d=0; d<4; d++){
                        int nx = x + rdx[d];
                        int ny = y + rdy[d];

                        if(nx <= 0) nx = N;
                        else if(nx > N) nx = 1;
                        if(ny <= 0) ny = M;
                        else if(ny > M) ny = 1;
                        if(nx == sx && ny == sy) return routeList;
                        
                        if(route[nx][ny] == item){
                            routeList.add(new int[]{nx, ny});
                            item--;
                            x = nx;
                            y = ny;
                            break;
                        }
                    }

                    if(x == sx && y == sy) break;
                }

                return null;
            }

            for(int d=0; d<4; d++){
                int nx = p.x + dx[d];
                int ny = p.y + dy[d];
                
                if(nx <= 0) nx = N;
                else if(nx > N) nx = 1;
                if(ny <= 0) ny = M;
                else if(ny > M) ny = 1;

                if(visited[nx][ny]) continue;
                if(isBroken[nx][ny]) continue;

                queue.offer(new Point(nx, ny, p.route));
                route[nx][ny] = route[p.x][p.y] + 1;
                visited[nx][ny] = true;
            }
        }

        return null;
    }

    static class Point{
        int x, y;

        public Point(int x, int y){
            this.x = x;
            this.y = y;
        }
    }
}

class Turret{
    int x; // 행
    int y; // 열
    int time; // 최근 공격 시간
    int power; // 공격력. 0 이하면 부서짐

    public Turret(int x, int y, int time, int power){
        this.x = x;
        this.y = y;
        this.time = time;
        this.power = power;
    }

    public String toString(){
        return x + " " + y + " " + time + " " + power;
    }
}