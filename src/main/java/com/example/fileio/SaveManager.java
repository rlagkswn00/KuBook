package com.example.fileio;

import com.example.SharedData;
import com.example.model.Date;
import com.example.model.KLog;
import com.example.model.Model;
import com.example.model.PenaltyUser;
import com.example.model.Reservation;
import com.example.utils.Validation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.example.fileio.FilePath.CURRENT_TIME_TXT;
import static com.example.fileio.FilePath.DATA_DIR;
import static com.example.fileio.FilePath.ETC_DIR;
import static com.example.fileio.FilePath.LOG_DIR;
import static com.example.fileio.FilePath.RESERVATION_DIR;

public class SaveManager {

    private BufferedWriter bw;
    public SharedData sharedData = SharedData.getInstance();

    // 현재 날짜
    private Date curTime;
    // 현재날짜 + 이후 7일(총 8일)
    private List<String> currentAndNextSevenDays;

    public SaveManager(Date curTime, List<String> currentAndNextSevenDays) {
        this.curTime = curTime;
        this.currentAndNextSevenDays = currentAndNextSevenDays;
    }

    /**
     * dirPath에 해당하는 디렉토리의 모든 파일을 삭제
     * @param dirPath : 디렉토리 경로
     */
    private void resetFilesInDir(String dirPath) {
        File dir = new File(dirPath);
        Validation.existDir(dir);
        if (!dir.isDirectory()) {
            throw new RuntimeException("directory인 경우에만 내부 파일들을 삭제할 수 있습니다.");
        }

        File[] files = dir.listFiles();

        // null 이면 이미 빈 디렉토리이므로 삭제할 필요x
        if (files == null) return;

        for (File file : files) {
            file.delete();
        }
    }

    /**
     * 유효한 날짜들(현재 시각 ~ 7일 후)에 관한 예약 정보를 파일에 저장
     */
    public void saveReservation() throws IOException {
        resetFilesInDir(RESERVATION_DIR);

        for (String date : currentAndNextSevenDays) {
            // 해당하는 날짜의 예약 정보들을 가져옴.
            List<Reservation> reservations = sharedData.reservationList.get(new Date(date));
            File file = new File(RESERVATION_DIR + date + ".txt");
            writeModelsToFile(file, reservations);
        }
    }

    /**
     * 유효한 날짜들(현재 시각 ~ 7일 후)에 관한 로그를 파일에 저장
     */
    public void saveLog() throws IOException {
        resetFilesInDir(LOG_DIR);

        for (String date : currentAndNextSevenDays) {
            // 해당하는 날짜의 로그들을 가져옴.
            List<KLog> kLogs = sharedData.logs.get(new Date(date));
            File file = new File(LOG_DIR + date + ".txt");
            writeModelsToFile(file, kLogs);
        }
    }

    /**
     * 사용자로부터 입력받은 현재 시각을 파일에 저장
     */
    public void saveCurrentTime() throws IOException {
        File dataDir = new File(DATA_DIR);
        Validation.existDir(dataDir);

        File file = new File(CURRENT_TIME_TXT);
        Validation.existFile(file);

        bw = new BufferedWriter(new FileWriter(file, false));
        bw.write(curTime.date + curTime.time);
        bw.flush();
        bw.close();
    }

    /**
     * 현재 날짜에 패널티가 적용된 유저 목록을 파일에 저장
     */
    public void savePenalty() throws IOException {
        File etcDir = new File(ETC_DIR);
        Validation.existDir(etcDir);
        // 패널티 파일 찾아서 삭제
        Arrays.stream(etcDir.listFiles())
                .filter(file -> file.getName().startsWith("p"))
                .findFirst().ifPresent(File::delete);

        List<PenaltyUser> penalizedUsers = sharedData.penalizedUsers.get(curTime);
        if (penalizedUsers == null) {
            return;
        }

        File file = new File(ETC_DIR + "p" + curTime.date + ".txt");
        writeModelsToFile(file, penalizedUsers);
    }

    /**
     * model들을 file에 쓰는 작업을 담당
     *
     * @param file   : 저장할 파일
     * @param models : 저장할 객체 리스트
     */
    private void writeModelsToFile(File file, List<? extends Model> models) throws IOException {
        // null이면 내용 저장 x
        bw = new BufferedWriter(new FileWriter(file));
        if (models == null) {
            bw.close();
            return;
        }
        for (Model model : models) {
            bw.write(model.toString());
            bw.newLine();
            bw.flush();
        }
        bw.close();
    }

}
