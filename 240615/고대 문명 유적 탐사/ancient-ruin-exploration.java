// 시작 240613 12:54~ 16:00
import java.util.*;
import java.io.*;
/*
1.탐사 진행
    a. 3X3 격자 선택해서 회전
        시계방향, 90도 or 180도 or 270도
        선택된 격자는 항상 회전

    b. 회전방법 중 [유물 1차 회득 가치 최대화 -> 회전 각도가 가장 작은 -> 열 가장 작은 -> 행 가장 작은] 방법 선택

2. 유물 획득
    a. 유물 1차 획득
        상하좌우 3개 이상이면 모여서 유물이 되어 사라진다. 유물의 가치 = 총 모인 조각수
        유적의 벽면(큐)으로 조각 채우기
            [열 작은 -> 행 큰] 순서로 조각 채운다.
            벽면의 숫자 사용하면 다시 사용 x
    b. 유물 연쇄 획득
        또 3개 이상 연결되면 또 유물되어 사라진다.
        또 새로 조각 채운다.
        3개 이상 연결 x 까지 반복

3. 탐사 반복
    1~2를 K번 반복
    각 턴마다 유물의 가치 총합 출력
    유물 획득x이면 종료

*/
public class Main {
    static int K, M;
    static int[][] map;
    static int[] wallArr;
    static int[] dx = {0, 1, 0, -1};
    static int[] dy = {1, 0, -1, 0};
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        st = new StringTokenizer(br.readLine());
        K = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());

        StringBuilder sb = new StringBuilder();

        map = new int[5][5];

        for(int i=0; i<5; i++){
            st = new StringTokenizer(br.readLine());
            for(int j=0; j<5; j++){
                map[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        wallArr = new int[M];

        st = new StringTokenizer(br.readLine());
        for(int i=0; i<M; i++){
            wallArr[i] = Integer.parseInt(st.nextToken());
        }
        int ans;
        boolean finish;
        Queue<Integer> tempWall = new LinkedList<>();
        for(int w : wallArr)
            tempWall.offer(w);
        while(K-- > 0){
            finish = true;
            ans = 0;

            // 탐사
            int[] way = proceedTamsa(map, wallArr);  // 1차획득, 회전각도, 중심x, 중심y

            rotation(way[2], way[3], map, way[1]);

            // 연쇄획득
            while(true){
                int area = acquire(map, tempWall);
                if(area > 0)
                    finish = false;
                if(area == 0) {
                    break;
                }
                
                ans += area;
            }
            if(!finish)
                sb.append(ans).append(" ");
            else
                break;
        }
        System.out.println(sb);
    }

    public static int[] proceedTamsa(int[][] map, int[] wallArr){
        // map, wall 복사
        int[][] temp = copyMap(map);
        ArrayList<int[]> wayList = new ArrayList<>();

        // 중심좌표 i,j
        for(int i=1; i<4; i++){
            for(int j=1; j<4; j++){
                // System.out.println("중심 " + i + ", " + j);
                for(int d=0; d<3; d++){ // 회전 각도 
                    // 회전
                    int[][] ttemp = copyMap(temp);
                    
                    rotation(i, j, ttemp, d);

                    // if(i==2 && j==2)
                    //     print(ttemp);
                    
                    // 1차 획득
                    int cnt = 0;
                    // if(i==2 && j==2)
                    boolean[][] visited = new boolean[5][5];
                    boolean[][] checked = new boolean[5][5];
                    int totalArea = 0;
                    for(int p=0; p<5; p++){
                        for(int q=0; q<5; q++){
                            if(visited[p][q]) continue;

                            int area = bfs(p, q, ttemp, visited);

                            if(area >= 3){
                                bfs(p, q, ttemp, checked);
                                totalArea += area;
                            }
                        }
                    }

                    // todo 회전 정보 저장
                    wayList.add(new int[]{totalArea, d, i, j}); // 1차획득, 회전각도, 중심x, 중심y
                }
            }
        }

        Collections.sort(wayList, (a, b) -> {
            if(a[0] == b[0]){
                if(a[1] == b[1]){
                    if(a[2] == b[2]){
                        return Integer.compare(a[3], b[3]);
                    }
                    return Integer.compare(a[2], b[2]);
                }
                return Integer.compare(a[1], b[1]);
            }
            return -Integer.compare(a[0], b[0]);
        });

        int[] way = wayList.get(0);

        return way;
    }

    public static int acquire(int[][] map, Queue<Integer> tempWall){
        boolean[][] visited = new boolean[5][5];
        boolean[][] checked = new boolean[5][5];
        int totalArea = 0;
        for(int i=0; i<5; i++){
            for(int j=0; j<5; j++){
                if(visited[i][j]) continue;

                int area = bfs(i, j, map, visited);

                if(area >= 3){
                    bfs(i, j, map, checked);
                    totalArea += area;
                }
            }
        }

        // 유물 조각 제거
        for(int i=0; i<5; i++){
            for(int j=0; j<5; j++){
                if(checked[i][j])
                    map[i][j] = 0;
            }
        }
        // System.out.println("유물 조각 제거");
        // print(map);
        // 조각 채우기
        for(int j=0; j<5; j++){
            for(int i=4; i>=0; i--){
                if(map[i][j] != 0 || tempWall.isEmpty()) continue;
            
                map[i][j] = tempWall.poll();
            }
        }
        // System.out.println("조각 채우기");
        // print(map);

        return totalArea;
    }

    public static int bfs(int i, int j, int[][] map, boolean[][] visited){
        Queue<Point> queue = new LinkedList<>();
        queue.offer(new Point(i, j));
        visited[i][j] = true;
        int area = 0;

        while(!queue.isEmpty()){
            Point p = queue.poll();
            area++;

            for(int d=0; d<4; d++){
                int nx = p.x + dx[d];
                int ny = p.y + dy[d];

                if(nx < 0 || nx >= 5 || ny < 0 || ny >= 5) continue;
                if(visited[nx][ny] || map[p.x][p.y] != map[nx][ny]) continue;

                queue.offer(new Point(nx, ny));
                visited[nx][ny] = true;
            }
        }

        return area;
    }

    public static void rotation(int x, int y, int[][] temp, int idx){

        // 90, 180, 270
        int time = 0;
        if(idx == 0){
            time = 2;
        }else if(idx == 1){
            time = 4;
        }else{
            time = 6;
        }

        while(time-- > 0){
            int i = x-1;
            int j = y-1;
            int d = 0;
            int item1 = temp[x-1][y-1];
            while(true){
                i += dx[d];
                j += dy[d];

                if(i < x-1 || i > x+1 || j < y-1 || j > y+1){
                    i -= dx[d];
                    j -= dy[d];
                    d = (d + 1) % 4;
                    i += dx[d];
                    j += dy[d];
                }

                int item2 = temp[i][j];
                temp[i][j] = item1;
                item1 = item2;

                if(i==x-1 && j==y-1) break;
            }
        }
    }

    public static int[][] copyMap(int[][] m){
        int[][] temp = new int[5][5];

        for(int i=0; i<5; i++){
            for(int j=0; j<5; j++){
                temp[i][j] = m[i][j];
            }
        }
        return temp;
    }

    public static void print(int[][] m){
        for(int i=0; i<5; i++){
            for(int j=0; j<5; j++){
                System.out.print(m[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}

class Point{
    int x, y;

    public Point(int x, int y){
        this.x = x;
        this.y = y;
    }
}