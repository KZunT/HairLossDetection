package com.jbmb.jbmb_coreserver.diagnosis.service;

import com.jbmb.jbmb_coreserver.account.jwt.JwtTokenProvider;
import com.jbmb.jbmb_coreserver.account.repository.MemberRepository;
import com.jbmb.jbmb_coreserver.diagnosis.domain.DiagnosisImage;
import com.jbmb.jbmb_coreserver.diagnosis.domain.DiagnosisLog;
import com.jbmb.jbmb_coreserver.diagnosis.domain.DiagnosisSurvey;
import com.jbmb.jbmb_coreserver.diagnosis.dto.Request.DisabledRequest;
import com.jbmb.jbmb_coreserver.diagnosis.dto.Request.HairLossBySurveyRequest;
import com.jbmb.jbmb_coreserver.diagnosis.dto.Request.ImageLinkRequest;
import com.jbmb.jbmb_coreserver.diagnosis.dto.Request.UpdateSurveyRequest;
import com.jbmb.jbmb_coreserver.diagnosis.dto.Response.HairLossBySurveyResponse;
import com.jbmb.jbmb_coreserver.diagnosis.dto.Response.Response;
import com.jbmb.jbmb_coreserver.diagnosis.dto.Response.UpdateSurveyResponse;
import com.jbmb.jbmb_coreserver.diagnosis.repository.ImageLinkRepository;
import com.jbmb.jbmb_coreserver.diagnosis.repository.UpdateLogRepository;
import com.jbmb.jbmb_coreserver.diagnosis.repository.UpdateSurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final UpdateLogRepository updateLogRepository;
    private final UpdateSurveyRepository updateSurveyRepository;
    private final ImageLinkRepository imageLinkRepository;
    private final Response response = new Response();
    private final UpdateSurveyResponse updateSurveyresponse = new UpdateSurveyResponse();
    private final HairLossBySurveyResponse hairLossBySurveyResponse = new HairLossBySurveyResponse();

    /**
     * 설문조사 하다가 중간에 튕겼을 때 삭제를 위한
     * resultCode 0:성공 , 1:진단기록 없음 , 2:아이디 틀림
     * @param DisabledRequest
     * @return Response
     */
    public Response disabledService(DisabledRequest disalbed){
        try{
            Integer userNum = memberRepository.findById(disalbed.getId()).get().getUserNum();
            Integer diagnosisID = updateLogRepository.findLogByUserNum(userNum);
            updateLogRepository.deleteById(diagnosisID);
            updateSurveyRepository.deleteById(diagnosisID);
        }catch (NoSuchElementException e){
            return response.builder().resultCode(2).build();
        }
        catch (Exception e){
            return response.builder().resultCode(1).build();
        }
        return response.builder().resultCode(0).build();
    }

    /**
     * 토큰 정보로 사용자 유저 번호를 가져옴
     * @param ServletRequest
     * @return userNum
     */
    private Integer getUserNum(ServletRequest request){
        Integer userNum;
        try {
            String id = jwtTokenProvider.getUserPk(jwtTokenProvider.resolveToken((HttpServletRequest) request));
            userNum = memberRepository.findById(id).get().getUserNum();
        }catch (Exception e){
            return 0;
        }
        return userNum;
    }

    /**
     * 설문조사 페이지 하나 넘길 때마다
     * checked 0:미체크 , 1:없다 , 2:있다
     * resultCode 0:성공 , 1:실패
     * @param UpdateSurveyRequest
     * @return UpdateSurveyResponse
     */
    public UpdateSurveyResponse updateService(ServletRequest request, UpdateSurveyRequest survey) {
        Integer userNum=getUserNum(request); // 유저 번호 받아오기
        if(userNum==0) return updateSurveyresponse.builder().resultCode(1).build(); // 사용자 번호가 존재하지 않을 경우
        Integer diagnosisID = updateLogRepository.findLogByUserNum(userNum); // 진단기록 가져오기
        if (diagnosisID == null) {  // 설문을 처음 진행할 경우
            diagnosisID = updateLogRepository.save(DiagnosisLog.builder()
                    .userNum(userNum)
                    .active(0)
                    .build()).getId();
            updateSurveyRepository.save(DiagnosisSurvey.builder()
                    .id(diagnosisID).build());
        }

        Optional<DiagnosisSurvey> result = updateSurveyRepository.findById(diagnosisID);
        int[] checkNum=new int[11];

        try{
        checkNum[1]=result.get().getSurvey1();
        checkNum[2]=result.get().getSurvey2();
        checkNum[3]=result.get().getSurvey3();
        checkNum[4]=result.get().getSurvey4();
        checkNum[5]=result.get().getSurvey5();
        checkNum[6]=result.get().getSurvey6();
        checkNum[7]=result.get().getSurvey7();
        checkNum[8]=result.get().getSurvey8();
        checkNum[9]=result.get().getSurvey9();
        checkNum[10]=result.get().getSurvey10();
        }catch (Exception e){}
        checkNum[survey.getSurveyNum()]= survey.getChecked();

        updateSurveyRepository.save(DiagnosisSurvey.builder()
                .id(diagnosisID)
                .survey1(checkNum[1])
                .survey2(checkNum[2])
                .survey3(checkNum[3])
                .survey4(checkNum[4])
                .survey5(checkNum[5])
                .survey6(checkNum[6])
                .survey7(checkNum[7])
                .survey8(checkNum[8])
                .survey9(checkNum[9])
                .survey10(checkNum[10])
                .build());

        return updateSurveyresponse.builder().resultCode(0).diagnosisID(diagnosisID).build();
    }

    /**
     * 이미지 링크 DB에 저장
     * resultCode 0:성공 , 1:실패
     * @param ImageLinkRequest
     * @return Response
     */
    public Response imageLinkService(ServletRequest request, ImageLinkRequest imageLink){
        Integer userNum=getUserNum(request); // 유저 번호 받아오기
        if(userNum==0) return response.builder().resultCode(1).build(); // 사용자 번호가 존재하지 않을 경우
        Integer diagnosisID = updateLogRepository.findLogByUserNum(userNum); // 진단기록 가져오기
        if(diagnosisID==null)return response.builder().resultCode(1).build();
        imageLinkRepository.save(DiagnosisImage.builder() // 이미지 링크 저장
                .id(diagnosisID)
                .diagnosisImage(imageLink.getImageLink())
                .build());
        updateLogRepository.save(DiagnosisLog.builder() // 이미지까지 저장했으므로 active를 1로
                .id(diagnosisID)
                .userNum(userNum)
                .active(1)
                .build());

        return response.builder().resultCode(0).build();
    }

    /**
     * DB에 저장된 설문 내용으로 설문 분석 리턴
     * resultCode 0:성공 , 1:실패
     * @param hairLossBySurvey
     * @return
     */
    public HairLossBySurveyResponse hairLossBySurveyService(HairLossBySurveyRequest hairLossBySurvey){
        Optional<DiagnosisSurvey> result = updateSurveyRepository.findById(hairLossBySurvey.getDiagnosisID());
        if(!result.isPresent()) return hairLossBySurveyResponse.builder().resultCode(1).build();
        Integer sum=result.get().getSurvey1()
                +result.get().getSurvey2()
                +result.get().getSurvey3()
                +result.get().getSurvey4()
                +result.get().getSurvey5()
                +result.get().getSurvey6()
                +result.get().getSurvey7()
                +result.get().getSurvey8()
                +result.get().getSurvey9()
                +result.get().getSurvey10()-10;
        if (sum<3) return hairLossBySurveyResponse.builder().resultCode(0).state(0).build();
        else if(sum<4) return hairLossBySurveyResponse.builder().resultCode(0).state(1).build();
        else if(sum<6) return hairLossBySurveyResponse.builder().resultCode(0).state(2).build();
        return hairLossBySurveyResponse.builder().resultCode(0).state(3).build();
    }
}