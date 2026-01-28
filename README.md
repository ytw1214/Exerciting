<h1>Exerciting</h1>

<img width="905" height="697" alt="image" src="https://github.com/user-attachments/assets/d2953bf9-952b-4bbc-bfcc-9089dc394874" />
<br>
1/26
<br>
팀간 승패표 데이터를 제외한 데이터를 가져오려 했지만 모든 테이블데이터를 가져오게 됨(수정중)
<br>
1/27
<br>
우선 kbo기준 크롤링을 진행했을때 10초 정도 걸리는것을 확인했고, 다른 종목까지 크롤링을 진행한다면 더 많은 시간이 소요되기 때문에 크롤링 속도를 향상시키고자 멀티쓰레드를 사용하려고 추상클래스를 도입해 각 종목마다의 크롤링 서비스를 추상클래스에서 상속받아 구현하려고 한다.
각 종목마다의 필드들이 다르다(야구에서는 게임차가 있지만 축구에서는 득실차, 배구에서는 세트 득실률과 같이) 이런 필드들을 Double로 묶어서 활용해야할거같다.
<br>
https://docs.spring.io/spring-batch/reference/scalability.html
https://jojoldu.tistory.com/493
멀티 스레드에 관련된 내용들
<br>
<img width="202" height="71" alt="image" src="https://github.com/user-attachments/assets/faaf3b2e-8225-4977-8df2-1da93292fc4e" />
<br>
멀티쓰레드 사용전 걸리는 시간 / 멀티스레드를 사용해 선수들의 기록도 저장해보고 차이를 작성할 계획.
