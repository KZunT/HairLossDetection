import 'package:flutter/material.dart';
import 'package:jbmb_application/screen/JoinPage.dart';
import 'package:jbmb_application/screen/LoginedHome.dart';
import 'package:jbmb_application/widget/JBMBBigLogo.dart';
import 'package:jbmb_application/widget/JBMBOutlinedButton.dart';
import 'package:jbmb_application/widget/JBMBTextField.dart';

class LoginPage extends StatelessWidget {
  const LoginPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {

    double phoneWidth = MediaQuery.of(context).size.width;
    double phoneHeight = MediaQuery.of(context).size.height;
    double phonePadding = MediaQuery.of(context).padding.top;

    return Scaffold(
      extendBodyBehindAppBar: true,
      backgroundColor: Colors.white,
      appBar: AppBar(
        elevation: 0,
        backgroundColor: Colors.transparent,
        leading: IconButton(
          icon: Icon(Icons.cancel),
          onPressed: () {
            FocusManager.instance.primaryFocus?.unfocus();
            Future.delayed(const Duration(milliseconds: 180), () {
              Navigator.pop(context);
            });
          },
        ),
        iconTheme: const IconThemeData(
          color: Colors.black,
        ),
      ),
      body: Container(
          alignment: Alignment.center,
          padding: EdgeInsets.all(phonePadding * 0.33),
          margin: EdgeInsets.all(phonePadding * 0.33),
          child: SingleChildScrollView(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                // TODO : Logo Image Resolution
                const JBMBBigLogo(),
                SizedBox(
                  height: phoneHeight * 0.043,
                ),
                SizedBox(
                    width: phoneWidth * 0.832,
                    child: JBMBTextField(
                      obsecure: false,
                      labelText: 'ID',
                      hintText: 'Enter your ID',
                    )),
                SizedBox(
                  height: phoneHeight * 0.021,
                ),
                SizedBox(
                    width: phoneWidth * 0.832,
                    child: JBMBTextField(
                      obsecure: true,
                      labelText: 'PW',
                      hintText: 'Enter your PW',
                    )),
                SizedBox(
                  height: phoneHeight * 0.021,
                ),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    JBMBOutlinedButton(
                      buttonText: '로그인',
                      iconData: Icons.login,
                      onPressed: () {
                        Navigator.pop(context);
                        Navigator.of(context).push(MaterialPageRoute(
                          builder: (context) => LoginedHome(),
                        ));
                      },
                    ),
                    JBMBOutlinedButton(
                      buttonText: '회원가입',
                      iconData: Icons.account_box,
                      onPressed: () {
                        FocusManager.instance.primaryFocus?.unfocus();
                        Future.delayed(const Duration(milliseconds: 180), () {
                          Navigator.pop(context);
                          Navigator.of(context).push(MaterialPageRoute(builder: (context) => JoinPage()));
                        });
                      },
                    ),
                  ],
                )
              ],
            ),
          )),
    );
  }
}
