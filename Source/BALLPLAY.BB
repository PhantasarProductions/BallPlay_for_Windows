AppTitle "BallPlay for Windows, Version 1.0 - Coded by Tricrokra"

;****************************************
;****************************************
;** BallPlay for Windows, V1.0         **
;** Coded by: Tricrokra                **
;** (C) 2001, JBC-Soft, Jeroen Broks   **
;** , Tricrokra, All Rights Reserved   **
;****************************************
;****************************************

;Yo guys. To whoever read this source code, welcome to it. As long as you leave the code unmodified, feel free to look through 
;it. It can be that this is coded a little messy, well, this game is basically meant to test if Blitz Basic would be the
;programming language for me. After years of coding in Turbo Pascal for DOS, it's very hard to find REAL game programming 
;friendly language for Windows. I really hope Blitz Basic is the language I need and this game will be to test that!
;Enjoy!!!

;If you got any comments on this product please e-mail me on: tricrokra@hotmail.com

;Okay some boring declarations

Graphics 640,480
Color 255,0,0
For AkB=0 To 25
	Line 0,AkB,640,AkB
	Next
Color 255,255,255	
Print "BallPlay 1.0 For Windows - Coded by: Tricrokra"
Print "Coded in BlitzBasic (Language developed by Mark Sibly)"
Print

Const ShowMouse = False   ; This is set in the debug version to check the coordinates. Should be false in the release

Const UltimateMax = 50

Global PackHeader$ = Chr(13)+"BallPlay For Windows 1.0 "+Chr(10)+Chr(13)+"(C) Copyright JBC-Soft, Jeroen Broks, 2002, All rights reserved"+Chr(26)

Dim Mission(50)     ; Used to set the level type. 1 = Normal, 2 = BreakAway, 3 = BreakFree, 4 = Color Splitting
Dim LevelName$(50)  ; Used to store the level name (Seems obvious) :)
Dim LvlBack(50)     ; Which Background are we using?
Dim Veld(50,16,11)  ; Used to store the level spot values
Dim Plate1(50)      ; How many plate / may you use?
Dim Plate2(50)      ; How many plate \ may you use?
Dim Wall(50)        ; How many walls may you use?
Dim Bulldozer(50)   ; How many bullsdozers may you use?
Dim Required(50)    ; Number of required balls to finish or to remain
Global MaxLevels    ; How many levels does this level pack contain?
Global Password$    ; Used to protect the custom level packs
Global PackName$    ; Name of the Level Pack
Global Author$      ; Who created the level pack?
Global Level        ; Which level are we playing/editing?
Global LevelPack$   ; Which level pack are we currently using to play / edit?

Global Score
Global Joker
Global Armageddon

;Game Messages
Type GameMessages 
	Field Message$
	Field Y
	End Type
Global GM.GameMessages

;Hi-Scores
Dim HiName$(10)
Dim HiScore(10)
Dim HiJoker(10)

;Missions
Dim MissionName$(4)
MissionName(1) = "Normal"
MissionName(2) = "Break Away"
MissionName(3) = "Break Free"
MissionName(4) = "Color Splitting"

;Font
Global Comic = LoadFont("Comic Sans MS",50)
Global Arial = LoadFont("Arial",15)

;Objects   (Old System)
Dim ObjType(30)   ; Used to store object type. 0 = Inactive, 1 = Ball, 2 = Robot, 3 = Ghost, 4 = Red Ball, 5 = Green Ball
Dim ObjWind(30)   ; Which way are we going? 1 = North, 2 = South, 3 = East, 4 = West
Dim ObjX   (30)   ; X Coordinate of object
Dim ObjY   (30)   ; Y Coordinate of object

;Objects   (New System)
Type Objects
	Field OType
	Field Wind
	Field X
	Field Y
	End Type

Global O.Objects
Global ObjAmmount = 0
Global ObjBalls = 0

;Balls In
Global BallsIn = 0
Global BallsDestroyed = 0

;A few game vars
Global Blink = 255
Global Obj = 1
Global DelObject
Dim    NME(16,11)

	

;Ball Play Logo Variables
Dim LogoY(9)      ; Used for title picture
Dim LogoW(9)      ; Used for title picture

;Load Images
Print "Loading Basic Images"
Global TitleLogo = LoadAnimImage("BALLS.Pcx",80,80,0,8)
Global ObjectImg = LoadAnimImage("CHECKBOA.PCX",40,40,0,150)
Global Mouse     = LoadImage    ("MOUSE.PCX")
Global Status    = LoadImage    ("STATUS.PCX")
Global Buttons   = LoadAnimImage("Buttons.PCX",80,17,0,21)
Global SmallBox  = LoadImage    ("SmallBox.Pcx")
Global HiScores  = LoadImage    ("Hi-Scores.pcx")

;Menu Items
Dim MenuOptions$(5)
MenuOptions$(1) = "Start Game"
MenuOptions$(2) = "Start Game From Custom Level Pack"
MenuOptions$(3) = "Level Editor"
MenuOptions$(4) = "High Scores"
MenuOptions$(5) = "Quit"

;Settings menu Variables
Dim VarSet$(12)

;Some other shit variables
Global A$ = ""        ;Used for level pack name memory
Global CurThing = 16  ;Used in the level editor to select the current thing...

;Load the Backgrounds
Const MaxBack = 20
Dim Background(20)
BackGround(0) = CreateImage(640,480)
For AkB=1 To MaxBack
    Print "Loading Background: "+"BACK"+Right("000"+AkB,4)+".PCX"
    Background(AkB) = LoadImage ("BACK"+Right("000"+AkB,4)+".PCX")
    If Background(AkB)=0 Then RuntimeError "Coudn't load Background #"+AkB
    MaskImage BackGround(AkB),255,255,255
    Next

;Init CD Tracks
Global MaxCD = 0
Global TestCD = 0
Write "Checking Audio CD ... "
CheckAudioCD
Print MaxCD+" tracks found"

;Load Sound Effects
Global Break     = LoadSound ("Break.Wav")
Global Explosion = LoadSound ("Explosion.Mp3")
Global Good      = LoadSound ("Good.Wav")

;Password Protection System Values
Global PWLen
Dim    PW(500)

;Max Ammount of Things in each category
Global MaxGen
Dim    MaxThings(16)

Restore Things
Read MaxGen
For AkB=1 To MaxGen
	Read MaxThings(AkB)
	Next

;Okay, er... Time for some main routines

Delay 1000
SetBuffer BackBuffer()
SeedRnd MilliSecs()
TitleScreen 
MainMenu
End

;And now for the routines itself, the game won't do much without them :)

Function IniFile$(Value$)
Local BT = ReadFile("BALLPLAY.INI")
Local Tmp$
Local Ret$ = "NOTHING AT ALL"
While Not Eof(BT)
	Tmp = Upper(ReadLine(BT))
	If Left$(Tmp,Len(Value)+1) = Upper(Value)+"=" Then
		Ret$ = Right(Tmp,(Len(tmp)-Len(Value))-1)
		EndIf
	Wend
CloseFile BT
Return Ret$
End Function

Function CheckLevel()
Local Ret = True
Local X,Y
Local Z.Objects
FlushKeys
Cls
Flip
Locate 0,0
SetFont Arial
If Required(Level) > OBJBalls Then
	Ret = False
	Print "ERROR: More balls required than there are in level (Balls:"+ObjBalls+" Req:"+Required(Level)+")"
	EndIf
For X=0 To 16 
	For Y = 0 To 11
		Select Veld(Level,X,Y)
			Case 112
				If Mission(Level)=2 Or Mission(Level)=4 Then 
					Print "ERROR: No normal exist allowed due to this mission ("+X+","+Y+")"
					ret = False
					EndIf				
			Case 113, 114
				If Mission(Level)=2 Or Mission(Level)=3 Or Mission(Level)=1 Then 
					Print "ERROR: No color exist allowed due to this mission ("+X+","+Y+")"
					Ret = False
					EndIf
			End Select
		Next
	Next
For Z.Objects = Each Objects
	If Z\OType = 16 And Mission(Level) = 4 Then Print "ERROR: No normal balls in colorsplitting allowed ("+Z\X+","+Z\Y+")"
	If (Z\OType = 19 Or Z\OType = 20) And Mission(level)<>4 And Mission(Level)<>2 Then Print "ERROR: No colored balls allowed in this mission"
	Next
If Not Ret Then
	FlushKeys
	Print "Press any key...
	WaitKey
	FlushKeys
	EndIf
Return Ret
End Function

	
Function CheckAudioCD()
Local Drive$ = IniFile$("CDPLAY")
Local BF
Local File$
Write "("+Drive+") ..."
BF = ReadDir(Drive)
MaxCD = 0

Repeat
	File$ = NextFile(BF)
	If Upper(File$)="" Then Exit
	If Upper(File$)="TRACK"+Right("0"+(MaxCD+1),2)+".CDA" Then MaxCD=MaxCD+1
	Forever
CloseDir BF
End Function
	
;Title Screen
Function TitleScreen()
Local Ak
For Ak=1 To 8
    LogoY(Ak)=Ak*10
    LogoW(Ak)=2
    Next 
DebugLog Comic
SetFont Comic
Repeat
Cls
Color 200,0,0
Text 100,1,"Welcome to:"
Text 100,240,"Version 1.0 for Windows"
Text 100,280,"Coded by: Tricrokra"
Text 150,400,"Press SPACEBAR to continue"
For Ak=1 To 8
    DrawImage TitleLogo,(AK-1)*80,LogoY(AK),Ak-1
    If LogoW(Ak)=1 Then
       LogoY(AK)=LogoY(ak)+1
       If LogoY(Ak)>150 Then LogoW(ak)=2
       EndIf
    If LogoW(Ak)=2 Then
       LogoY(AK)=LogoY(ak)-1
       If LogoY(Ak)<10 Then LogoW(ak)=1
       EndIf

    Next 
Delay 1
Flip
Until KeyHit(57)
End Function

;Main Menu
Function MainMenu()
Local EndGame = False
Local Ak
Local P=1
Local MS = False
Local MsS = 0
Local Down = False
Local Up   = False
Local Sinus = 0
FlushKeys
For Ak=1 To 30      ;Init flying objects
    ObjType(ak)=Rnd#(16,20)
    ObjX(Ak)=Rnd#(600)
    ObjY(ak)=Rnd#(440)
    ObjWind(Ak)=Rnd#(10)+1
    Next   
Repeat 
Cls
SetFont Comic
;Draw Screen
For Ak=1 To 30      ;Flying Objects
    ObjY(Ak)=ObjY(Ak)+ObjWInd(Ak)
    If ObjY(Ak)>480 Then 
       ObjY(AK)=-50
       ObjX(AK)=Rnd#(600)
       ObjType(Ak)=Rnd#(16,20)
       ObjWind(Ak)=Rnd#(10)+1
       EndIf
    DrawImage ObjectImg,ObjX(Ak),ObjY(Ak),ObjType(AK)
    Next
For Ak=1 To 8       ;Init BallPlay Logo
    DrawImage TitleLogo,(AK-1)*80,Int(Sin((Ak*20)+Sinus)*100)+100,Ak-1
    Sinus = Sinus + 1
    If Sinus = 360 Then Sinus = 0
    Next            ;End this shit
;Menu Options
;MS=MilliSecs()
;MS=Int(MS/10)
MsS=MsS+1
If MsS>10 Then
   MS=Not(MS)
   MsS=0
   EndIf
For AK=1 To 5
    Color 200,200,200
    If (Ak=P) And (MS) Then Color 200,0,0
    Text 320,250+(AK*30),MenuOptions$(Ak),True
    Next
Flip 
;Check Keypresses
If KeyHit(1) Then EndGame=True
If KeyDown(200) And (Not(Up)) Then
   Up=True
   P=P-1
   If P=0 Then P=5
   EndIf
If KeyDown(208) And (Not Down) Then
   Down=True
   P=P+1
   If P=6 Then P=1
   EndIf
If Not KeyDown(200) Then Up=False
If Not KeyDown(208) Then Down=False
If (KeyDown(28)) And (P=1) Then StandardLevels
If (KeyDown(28)) And (P=2) Then CustomLevels
If (KeyDown(28)) And (P=3) Then LevelEditor
If (KeyDown(28)) And (P=4) Then 
	LevelPack$ = "BALLPLAY.LVL" 
	HiScores
	EndIf
If (KeyDown(28)) And (P=5) Then EndGame=True
Until EndGame
End Function

Function StandardLevels()
LevelPack$ = "BALLPLAY.LVL"
Game
End Function

Function CustomLevels()
EnterPackName
LevelPack$ = A$ + ".BPW"
Game
End Function

Function DrawLevel()
Local AX,AY
;DebugLog "Level = "+Level+"     LvlBack("+Level+") = "+LvlBack(Level)+"   -> Background = "+Background(LvlBack(Level))
DrawImage Background(LvlBack(Level)),0,0
For AX = 0 To 16
	For AY = 0 To 11
		DrawImage ObjectImg,(AX*40),(AY*40),Veld(Level,AX,AY)
		Next
	Next
DrawImage Status,560,0	
End Function

Function DataButtons()
Local Loc, Ammount
Local Butt
Local Ak
Read Loc
Read Ammount
For Ak=1 To Ammount
	Read Butt
    DrawImage Buttons,560,Loc,Butt
	Loc = Loc + 17
	Next
End Function

Function EditDrawScreen()
DrawLevel
Restore LevelEditorData
DataButtons
MousePointer
SetFont Arial
Text 560,200,"Level:"
Text 560,215,Level+"/"+MaxLevels
Text 560,245,"BackGround:"
Text 560,260,LvlBack(Level)+"/"+MaxBack
SetFont Comic
End Function

Function CMX()
Return Int(MouseX()/40)
End Function

Function CMY()
Return Int(MouseY()/40)
End Function

Function MousePointer()
Color 255,255,255
DrawImage Mouse,MouseX(),MouseY()
If ShowMouse Then 
	Text 0,380, "Mouse Position: ("+MouseX()+","+MouseY()+")"
	If MouseX()<560 Then Text 0,430, "Cursor Position: ("+CMX()+","+CMY()+")"
	EndIf
End Function

Function AmmInput(St$)
Local Question$ = "Please enter the maximum ammount of "+St$+": "
Local Max = 99
Cls
Flip
FlushKeys
If St$="REQ" Then 
  	Question="How many balls are required to finish this level? "
	Max = 30
	EndIf
Repeat	
Local Value = Input(Question$)
If Value>Max Then Print "Please enter a number between 0 and "+Max
If St$="REQ" And Value=0 Then Value = 1
Until Value <= Max
FlushKeys
Return Value
End Function

Function SuperInput$(Question$)	
Local V$
FlushKeys
Cls
V$ = Input(Question$)
FlushKeys
Return V$
End Function

Function LevelSettings()
Local Ak
Local P = 1
FlushKeys
Repeat
Locate 0,0
VarSet(01) = "Level Name: "+LevelName(Level)
VarSet(02) = "Mission: "+MissionName(Mission(Level))
VarSet(03) = "Background: "+LvlBack(Level)+"/"+MaxBack
VarSet(04) = "Plate /: "+Plate1(Level)
VarSet(05) = "Plate \: "+Plate2(Level)
VarSet(06) = "Walls: "+Wall(Level)
Varset(07) = "Bulldozers: "+Bulldozer(Level)
VarSet(08) = "Balls required for success: "+Required(Level)
VarSet(09) = "Name Of This Level Pack: "+PackName
VarSet(10) = "Creator of This Level Pack: "+Author
VarSet(11) = "Password for this level pack: "+Password
VarSet(12) = "Back to the editor"
DrawImage Background(LvlBack(Level)),0,0
Color 255,255,255
SetFont Comic
Text 320,0,"Settings",True
SetFont Arial
For Ak=1 To 12
	Color 255,255,255
	If AK = P Then Color Rand(0,255),Rand(0,255),Rand(0,255)
	Text 50,(Ak*14)+50,VarSet(Ak)
	Next
Flip
If KeyHit(28) Then
	Select P
	    Case 01 
				Cls
				FlushKeys
				Flip
				LevelName(Level) = Input("Please enter the name for this level:")
				FlushKeys
		Case 02
				Mission(Level) = Mission(Level) + 1
				If Mission(Level) = 5 Then Mission(Level) = 1							
		Case 03    
				If LvlBack(Level)<>0 Then
					LvlBack(Level) = LvlBack(Level) + 1
					If LvlBack(Level)>MaxBack Then LvlBack(Level) = 1
					EndIf									
		Case 04 Plate1(Level)    = AmmInput("Plates /")
		Case 05 Plate2(Level)    = AmmInput("Plates \")
		Case 06 Wall  (Level)    = AmmInput("Walls")
		Case 07 Bulldozer(Level) = AmmInput("Bulldozer")
		Case 08 Required(Level)  = AmmInput("REQ")
		Case 09 PackName         = SuperInput("Please give a name for this level pack: ")
		Case 10 Author           = SuperInput("Please enter your name or nick:" )
		Case 11 Password         = SuperInput("Please enter a password for this level pack: ")
		Case 12 Exit
		End Select
	EndIf
If KeyHit(200) Then P = P - 1
If KeyHit(208) Then P = P + 1
If P = 00 Then P = 12
If P = 13 Then P = 01
Color 255,255,255
Until KeyDown(1)
FlushMouse
End Function

Function CenterImage(Img)
Local SX = ImageWidth(Img)
Local SY = ImageHeight(Img)
SX = 320 - Int(SX/2)
SY = 240 - Int(SY/2)
DrawImage Img,SX,SY
End Function

Function Things()
Local CG = Int(CurThing/16)
Local CT = CurThing - (CG*16)
Local Thingy
FlushKeys
FlushMouse
Repeat
EditDrawScreen
CenterImage SmallBox
Thingy = (CG*16) + CT
DrawImage Buttons  ,355,205,3
DrawImage Buttons  ,355,222,18
DrawImage Buttons  ,355,239,19
MousePointer
DrawImage ObjectImg,205,205,Thingy
Color 0,0,0
Text 280,205,CG+"."+Right("0"+CT,2)   ;+" "+Thingy
If MouseX()>353 And MouseX()<435 And MouseHit(1) Then
	If MouseY()>205 And MouseY()<222 Then 
		If MouseX()<393 Then CT = CT - 1 Else CT = CT +1
		EndIf
	If MouseY()>222 And MouseY()<239 Then 
		CurThing = (CG*16) + CT
		Exit
		EndIf
	If MouseY()>240 And MouseY()<257 Then Exit
	EndIf
If CT=-1 Then
	CG = CG - 1
	If CG = 0 Then CG = MaxGen
	CT = MaxThings(CG)
	EndIf
If CT>MaxThings(CG) Then
	CT = 0
	CG = CG + 1
	If CG > MaxGen Then CG = 1
	EndIf			
Flip
If KeyHit(1) Then Exit
Forever
While MouseDown(1) Wend
While MouseDown(2) Wend
FlushMouse
FlushKeys
End Function

Function TestLevel()
SaveLevelPack
Score = 0
Local Result = PlayLevel(True)
LoadLevelPack
While MouseDown(1) Wend
End Function

Function AddLevel()
If MaxLevels >= UltimateMax Then
	Locate 0,0
	SetFont Comic
	Print "Can't create anymore levels!!!
	Delay 5000
	Return
	EndIf
If YesNo("Create a new level?") Then
	CreateLevel	
	Level = MaxLevels
	EndIf
End Function

Function RemoveLevel()
Local Ak,XX,YY
If MaxLevels = 1 Then 
	PlaySound Explosion
	Return
	EndIf
If YesNo("Remove Level #"+Level+"?") Then
	For Ak= Level To UltimateMax-1 
	Mission(Ak)    = Mission(Ak+1)
	LevelName$(Ak) = Levelname(Ak+1)
	LvlBack(Ak)    = LvlBack(Ak+1)
	Plate1(Ak)     = Plate1(Ak+1)
	Plate2(Ak)     = Plate1(AK+1)
	Wall(Ak)       = Wall(Ak+1)
	Bulldozer(Ak)  = Bulldozer(ak+1)
	Required(AK)   = Required(Ak+1)
	For XX=0 To 16 For YY=0 To 11
		Veld(AK,XX,YY) = Veld(Ak+1,XX,YY)
		Next Next
	Next
	EndIf
MaxLevels = MaxLevels - 1
If Level>MaxLevels Then Level = MaxLevels
FlushMouse
End Function

Function EditorOptions()
Local Y=MouseY()
If Y>=000 And Y<=018 Then Things
If Y>=019 And Y<=033 Then LevelSettings
If Y>=034 And Y<=050 Then SaveLevelPack
If Y>=050 And Y<=068 Then If YesNo("Reload the level pack") Then LoadLevelPack
If Y>=068 And Y<=084 Then TestLevel
If Y>=084 And Y<=102 Then AddLevel
If Y>=102 And Y<=120 Then RemoveLevel
If Y>=120 And Y<=137 Then
	If MouseX()<598 Then Level = Level - 1 Else Level = Level + 1
	If Level = 0 Then Level = MaxLevels
	If Level > MaxLevels Then Level = 1
	EndIf
If Y>=137 And Y<=154 Then Return False
Return True
End Function

Function SaveLevelPack()
Local Ak,Al,Lv
Local BT = WriteFile(LevelPack$)
Local Process#
DebugLog LevelPack$+" -> "+BT
If BT = 0 Then
	Cls
	FlushKeys
	Flip
	SetFont Arial
	Print "I'm very sorry, but for some silly reasons"
	Print "I can't write to file "+LevelPack$
	Print "The file name could be invalid, or there's a"
	Print "Disk error!"
	Print
	Print "Press any key..."
	WaitKey
	Return
	EndIf
EditDrawScreen
CenterImage SmallBox
Color 0,0,0
Text 320,240,"Saving...",True,True
Flip
CodePassword
WriteByte   Bt,1                  ; Version x.y    write x
WriteByte   Bt,0                  ; Version x.y    write y
WriteString Bt,PackHeader
WriteString Bt,PackName$
WriteString Bt,Author
WriteInt    Bt,PWLen
For Ak=1 To PWLen
	WriteInt Bt,PW(Ak)
	Next
WriteInt    Bt,MaxLevels
SetBuffer FrontBuffer()
Color 100,100,100
Rect 0,440,640,480
For Lv = 1 To MaxLevels
	Process# = (Lv / MaxLevels)
	Color 255-(Int(Process*255)),Int(Process*255),0
	Rect 0,440,Int(Process*640),480
	;DebugLog "Saving level: "+Lv
	WriteString BT,LevelName(Lv)
	WriteInt    Bt,Mission(Lv)
	WriteInt    Bt,LvlBack(Lv)
	WriteByte   Bt,Plate1(Lv)
	WriteByte   Bt,Plate2(Lv)
	WriteByte   Bt,Wall(Lv)
	WriteByte   Bt,Bulldozer(Lv)
	WriteByte   Bt,Required(Lv)
	For Ak = 0 To 16
		For Al = 0 To 11
			;DebugLog "Saving Veld "+Ak+","+Al
			WriteByte BT,Veld(Lv,Ak,Al)
			Next
		Next
	Next
CloseFile Bt
SetBuffer BackBuffer()
End Function

Function Game()
Local Continue
If Not LoadLevelPack() Then Return
Level = 1
Joker = 5
Score = 0
Repeat
LoadLevelPack
Continue = PlayLevel(False)
If Level>MaxLevels Then 
	EnterHiScore
	Continue = False
	EndIf
Until Not Continue
HiScores
End Function

Function ScreenShot()
Local Shot = 0
If KeyHit(68) Then
	While FileType("BPSHOT_"+Shot+".BMP")=1 
		Shot = Shot + 1
		Wend
	SaveBuffer FrontBuffer(),"BPSHOT_"+Shot+".BMP"
	SetFont Arial
	Print "Screenshot 'BPSHOT_"+Shot+".BMP' saved"
	Delay 5000
	EndIf
End Function


Function HiScores()
Local SinX# = 0
Local SinY# = 0
Local Ak
LoadHiScores
SetFont Comic
Color 255,255,255
FlushKeys
FlushMouse
Repeat
	DrawImage Background(14),0,0
	DrawImage HiScores,Int(Sin(SinX)*30)+30,Int(Sin(SinY)*180+180)
	SinX# = SinX# + 0.3
	SinY# = SinY# + 1.0
	If SinX>360 Then SinX = SinX-360
	If SinY>360 Then SinY = SinY-360
	For Ak=1 To 10
		Text 010,(Ak*40),HiName$(Ak)
		Text 400,(Ak*40),HiScore(Ak),True
		Text 560,(Ak*40),HiJoker(Ak),True
		Next
	Delay 1
	Flip
	Until KeyHit(1) Or MouseHit(1)
FlushKeys	
FlushMouse
End Function

Function EnterHiScore()
Local YourName$
Local Ak
Local Place = 11
Local Col#
LoadHiScores
For Ak = 10 To 1 Step -1
	If HiScore(Ak)<Score Then Place = Ak
	Next
If Place<11 Then
	Col# = Place / 10
	Color Int(Col#*255),255 - Int(Col#*255),0
	SetFont Comic
	Cls
	Flip
	Locate 0,0
	Print "Congratulations!"
	Print "You got a Hi-Score"
	Print "You are on rank #"+Place
	YourName$ = Input("Enter your name please: ")
	If Len(YourName$)>10 Then YourName$ = Left(YourName$,10)
	If Place<10 Then
		For Ak=9 To Place Step -1
			HiName$(Ak+1)=HiName$(Ak)
			HIScore(Ak+1)=HiScore(Ak)
			HiJoker(Ak+1)=HiJoker(Ak)
			Next
		EndIf
	HiName$(Ak+1) = YourName$
	HiScore(Ak+1) = Score
	HiJoker(AK+1) = Joker
	SaveHiScores
	EndIf
End Function

Function SaveHiScores()
Local Ak
Local BT = WriteFile(LevelPack$+".Hi")
For Ak = 1 To 10
	WriteString BT,HiName$(Ak) 
	WriteInt    BT,HiScore(Ak)
	WriteByte   Bt,HiJoker(Ak)
	Next
CloseFile BT
End Function

Function LoadHiScores()
Local Ak
Local BT = ReadFile(LevelPack$+".Hi")
HiName$(1) = "BallPlay"
HiName$(2) = "A game by:"
HiName$(3) = "Tricrokra"
HiName$(4) = "Greetings to:"
HiName$(5) = "Aeris
HiName$(6) = "Wicky77"
HiName$(7) = "HuKe"
HiName$(8) = "Raikazoku"
HiName$(9) = "And all members"
HiName$(10) = "of Kerosine"
For Ak = 1 To 10 
	HiJoker(Ak) = 11 - Ak
	HiScore(Ak) = 1100 - (AK*100)
	Next
If BT<>0 Then
	For Ak = 1 To 10
		HiName$(Ak) = ReadString(BT)
		HiScore(Ak) = ReadInt(BT)
		HiJoker(Ak) = ReadByte(BT)
		Next
	CloseFile BT
	EndIf
End Function

Function LoadLevelPack()
Local BT  = ReadFile(LevelPack$)
Local Lvl
Local Ak,Al
Local V1,V2
Local H$
Delete Each Objects
SetFont Arial
If Bt = 0 Then
	Cls
	Flip
	Print "Hmmm... I can't load: "+LevelPack$
	Print "You could have mispelled the filename, or there's a diskerror"
	Print "I'm very very sorry, pal!"
	Print
	Print "Jam any key to go back to the main menu!"
	FlushKeys
	WaitKey
	FlushKeys
	Return False
	EndIf
V1 = ReadByte(BT)
V2 = ReadByte(BT)
If V1<>1 And V2<>0 Then
	Cls
	Flip
	Print "This level pack is created with a diffrent version"
	Print "It may contain codes, this version does not support!"
	Print
	Print
	Print "Jam any key to contine...."
	FlushKeys 
	WaitKey 
	FlushKeys
	Return False
	EndIf
H$ = ReadString(BT)
If H$<>PackHeader$ Then
	Cls
	Flip
	Print "This file is either corrupted, or somebody tried to be funny with it!"
	Print
	Print "Jam any key..."
	FlushKeys
	WaitKey
	FlushKeys
	Return False		
	EndIf
PackName$   = ReadString(BT)
Author$	    = ReadString(BT)
PwLen       = ReadInt(BT)
For Ak = 1 To PwLen
	Pw(Ak)  = ReadInt(BT)
	Next
MaxLevels   = ReadInt(BT)
If MaxLevels > UltimateMax Then MaxLevels = 50
For Lv = 1 To MaxLevels
	LevelName (Lv) = ReadString(BT)
	Mission   (Lv) = ReadInt   (BT)
	LvlBack   (Lv) = ReadInt   (BT)
	Plate1    (Lv) = ReadByte  (BT)
	Plate2    (Lv) = ReadByte  (BT)
	Wall      (Lv) = ReadByte  (BT)
	Bulldozer (Lv) = ReadByte  (BT)
	Required  (Lv) = ReadByte  (BT)
	For Ak = 0 To 16
		For Al = 0 To 11
			Veld(Lv,Ak,Al) = ReadByte(BT)
			Next
		Next
	Next
CloseFile BT
Return True
End Function	

Function LevelEditor()
FlushKeys
EnterPackName$
LevelPack$ = A$ + ".BPW"
If FileType(LevelPack$)=0 Then
	Color 0,200,0
	Text 0,120,"Oh, I see that level pack does not exist?"
	Text 0,150,"Do you want to create a new pack?"
	Text 0,180,"Press 'y' for yes, any other key is 'no'"
	Flip
	If Upper(Chr(WaitKey()))<>"Y" Then Return
	MaxLevels = 0
	Password=""
	CreateLevel
	Else 
	If Not(LoadLevelPack()) Then Return
	If PWLen<>0 Then
		Cls
		Flip
		Locate 0,0
		Print "This pack is password protected"
		PassWord = Input("Please enter password: ")
		If Not PassWord = DeCodePassword(PassWord) Then 
			Color 200,0,0
			Print "Incorrect Password!"
			Color 0,200,200
			Print
			Print "Press any key..."
			WaitKey
			FlushKeys
			While KeyDown(28) Wend
			Return
			EndIf
		EndIf
	EndIf	
FlushKeys
Level = 1
FlushMouse 	
Repeat
EditDrawScreen
Flip
ScreenShot
If KeyHit(1) Then Exit
If MouseX()<560 And MouseDown(1) Then 
	Veld(level,CMX(),CMY()) = CurThing
	;DebugLog "Edit "+CMX()+" "+CMY()+" -> "+CurThing
	EndIf
If MouseX()<560 And MouseDown(2) Then 
	Veld(Level,CMX(),CMY()) = 0
    EndIf	
If MouseX()>560 And MouseHit(1) Then 
	If (Not EditorOptions()) Then Exit
	EndIf
Forever	
SaveLevelPack
FlushKeys
FlushMouse
End Function 

Function CreateLevel()
Local Ak,Al
MaxLevels = MaxLevels + 1
LevelName(MaxLevels) = "< No Name >"
For Ak=0 To 16 
 	For Al=0 To 11
		Veld(MaxLevels,Ak,Al)=0
		Next
	Next
Plate1   (MaxLevels) = 0
Plate2   (MaxLevels) = 0
Wall     (MaxLevels) = 0
Bulldozer(MaxLevels) = 0
Required (MaxLevels) = 0
Mission  (MaxLevels) = 1
LvLBack  (MaxLevels) = Rand(1,MaxBack)
;If MaxLevels=50 Then LvlBack(MaxLevels)=0
End Function


Function EnterPackName$()
Local X = 1
FlushKeys
Repeat
Cls
Color 0,200,0
Text 0,0,"Please enter the name of"
Text 0,30,"the desired level pack:"
Color 0,200,200
Text 0,60,A$+"_"
Flip
X=WaitKey()
If (Chr$(X)="?") Or (Chr$(X)="*") Or (Chr$(X)="/") Or (X=34) Then X=0
If (X>31) And (Len(A$)<10) Then A$=A$+Chr$(X)
If (X=8) And (A$<>"") Then A$=Left$(A$,Len(A$)-1)
Until X=13
Cls
For X = 1 To 2
	Color 0,200,0
	Text 0,0,"Please enter the name of"
	Text 0,30,"the desired level pack:"
	Color 0,200,200
	Text 0,60,A$
	Flip
	Next
If A$="" Then A$="CUSTOM"
;Return A$
;SetBuffer BackBuffer
Repeat
Until Not(KeyDown(28))
End Function

Function CodePassword()
Local AK
PWLen = Len(Password$)
For Ak=1 To PWLen
	PW(Ak)=Asc(Mid$(Password$,Ak,1))+Asc(Mid$(Password$,(PwLen+1)-AK,1))	
    ;Print Mid$(Password$,Ak,1)+"   "+Mid$(Password$,(PwLen+1)-AK,1)+"   "+Asc(Mid$(Password$,Ak,1))+"   "+Asc(Mid$(Password$,(PwLen+1)-AK,1))	+"   "+Pw(Ak)
	Next
End Function	

Function DeCodePassword$(P$)
Local Ret$ = ""
Local C
Local Ak
If PWLen<>Len(P$) Then Return "!Error!"
For Ak=1 To PWLen
	C = PW(Ak) - Asc(Mid$(P$,(PwLen+1)-Ak,1))
	If C<0 Then 
		Ret$ = "Error"
		Else
		Ret$ = Ret$ + Chr(C)
		EndIf
	;Print Ret$
	Next
Return Ret
End Function	

Function Col(C1,C2,Bl)
If C1 = C2 Then Color Bl,Bl,Bl Else Color 255,255,255
End Function

Function PlayDrawScreen()
Local Ob.Objects
DrawLevel
;Draw Objects
For Ob.Objects = Each Objects
	DrawImage ObjectImg,(Ob\X)*40,(Ob\Y)*40,Ob\OType	
	;DebugLog "X="+O\X+"   Y="+O\Y+"   Type = "+O\OType
	Next
;Draw Options
DrawImage ObjectImg,570,020,1
DrawImage ObjectImg,570,080,2
DrawImage ObjectImg,570,140,3
DrawImage ObjectImg,570,200,4
DrawImage ObjectImg,570,260,5
SetFont Arial
;The objects the player can use
Col 1,Obj,Blink    Text 595,040,Plate1(Level)
Col 2,Obj,Blink    Text 595,100,Plate2(Level)
Col 3,Obj,Blink    Text 595,160,Wall(Level)
Col 4,Obj,Blink    Text 595,220,Bulldozer(Level)
Color 255,255,255  Text 595,280,Joker
;Data
If Score<0 Then Score = 0
Text 560,305,"Level:"+Level
Text 560,320,"Mission:"+Mission(Level)
Text 560,335,"Score: "+Score
Text 560,350,"Balls: "+ObjBalls
Text 560,365,"Destroyed:"+BallsDestroyed
If Mission(Level)<>2 Then
	Text 560,380,"Arrived: "+BallsIn
	End If
Restore PlayData
DataButtons
End Function

Function CreateObjects()
Local XX,YY
ObjBalls = 0
Delete Each Objects
For XX = 0 To 16
	For YY = 0 To 11
		If Veld(Level,XX,YY) > 15 And Veld(Level,XX,YY) < 32 Then
			O.Objects = New Objects
			O\OType = Veld(Level,XX,YY)
			O\X     = XX
			O\Y     = YY
			O\Wind  = 2
			Veld(Level,XX,YY) = 0
			ObjAmmount = ObjAmmount + 1
			If O\OType <> 17 And O\OType <> 18 Then ObjBalls = ObjBalls + 1
			EndIf
		Next
	Next
End Function	

Function IntroduceLevel(Test)
Local Ret = 0
FlushMouse
FlushKeys
CreateObjects
If Not CheckLevel() Then Return 2
Repeat
PlayDrawScreen
DrawImage Buttons,010,200,9
DrawImage Buttons,090,200,4
If Not Test Then
	DrawImage Buttons,010,220,0
	DrawImage Buttons,090,220,1
	EndIf
MousePointer
SetFont Comic
Text 0,000,"Level: "+Level
Text 0,035,LevelName(Level)
Text 0,070,"Number Of Balls: "+ObjBalls
Text 0,105,"Balls Required: "+Required(Level)
Text 0,140,"Mission: "+MissionName(Mission(Level))
Flip
If KeyHit(01) Then Ret = 2
If KeyHit(28) Then Ret = 1
If MouseHit(1) Then
	If MouseY()>199 And MouseY()<215 And MouseX()>10 And MouseX()<089 Then Ret = 1
	If MouseY()>199 And MouseY()<215 And MouseX()>90 And MouseX()<170 Then Ret = 2
	If Not Test Then
		If MouseY()>220 And MouseY()<237 And MouseX()>010 And MouseX()<090 Then SaveGame 0
		If MouseY()>220 And MouseY()<237 And MouseX()>090 And MouseX()<170 Then LoadGame 
		EndIf
	EndIf
Until Ret>0
PlayDrawScreen 
Flip
While MouseDown(1) Wend
While MouseDown(2) Wend
Return Ret
End Function

;Return Codes
; 1 = Play Level
; 2 = Cancel
; 3 = Save Game (Not in test mode)
; 4 = Load Game (Not in test mode)

Function Block(X,Y)
If X<00 Then Return True
If X>13 Then Return True
If Y<00 Then Return True
If Y>11 Then Return True
If Veld(Level,X,Y)>=64 And Veld(Level,X,Y)<=79 Then Return True
Return False
End Function

Function SaveGame(L)
FlushKeys
Cls 
SetFont Comic
Text 0,0,"Save Game:"
Text 0,60,"Please give a letter for the savegame:"
Flip
Local Letter$ = Upper(Chr(WaitKey()))
If Asc(Letter)>=Asc("A") And Asc(Letter)<=Asc("Z") Then
	Local BT = WriteFile(LevelPack$+".SG"+Letter)
	WriteInt(BT,Score)
	WriteInt(BT,Level+L)
	WriteInt(BT,Joker)
	CloseFile BT
	Cls
	Text 0,0,"Saved in '"+Letter$+"'"
	Flip
	Delay 1000
	EndIf
FlushKeys
End Function

Function LoadGame()
FlushKeys
Cls 
SetFont Comic
Text 0,0,"Load Game:"
Text 0,60,"Please give a letter for the savegame:"
Flip
Local Letter$ = Upper(Chr(WaitKey()))
If Asc(Letter)>=Asc("A") And Asc(Letter)<=Asc("Z") Then
	Local BT = ReadFile(LevelPack$+".SG"+Letter)
	If BT = 0 Then
		Cls
		Text 0,0,"Couldn't load file! ("+Letter$+")"
		Flip
		Delay 1000
		Return
		EndIf
	Score = ReadInt(BT)
	Level = ReadInt(BT)
	Joker = ReadInt(BT)
	CloseFile BT
	LoadLevelPack
	CreateObjects
	Cls	
	Text 0,0,"Loaded from '"+Letter$+"'"
	Flip
	Delay 1000
	EndIf
FlushKeys
End Function

Function BlockFront(X,Y,Wind)
Select Wind
	Case 1  Return Block(X,Y-1)
	Case 2  Return Block(X,Y+1)
	Case 3  Return Block(X+1,Y)
	Case 4  Return Block(X-1,Y)
	Default RuntimeError "BlockFront: Incorrect Wind Direction ("+Wind+")"
	End Select
End Function

Function LevelOver(Test)
Local Balls = BallsIn
Local Gelukt = False
Local Ok = False
Local ReqJoker
FlushKeys
FlushMouse
;If Mission(Level) = 2 Then Balls = ObjBalls
If Balls >= Required(Level) Then
	Gelukt = True
	Score = Score + 10
	If BallsDestroyed = 0 Then Score = Score + 15
	Else
	ReqJoker = Required(Level) - Balls
	End If
Repeat
PlaydrawScreen
If Test Then
	DrawImage Buttons,20,160,11
	Else
	If Gelukt Then
		DrawImage Buttons,20,160,20
		Else
		DrawImage Buttons,20,160,13
		If ReqJoker<=Joker Then 
			SetFont Arial
		    Jok$="You need "+ReqJoker+" joker"
		    If ReqJoker>1 Then Jok$=Jok$+"s"
			Text 360,160,Jok$
			DrawImage Buttons,260,160,15
			EndIf
		EndIf
	If Level<MaxLevels Then DrawImage Buttons,100,160,0
	DrawImage Buttons,180,160,4
	EndIf
MousePointer
SetFont Comic
If Gelukt Then Text 0,0,"All right! You made it!" Else Text 0,0, "Too bad! Better luck next time"
Text 0,055,"Successful balls: "+BallsIn
Text 0,110,"Required: "+Required(Level)
Flip
If KeyHit(1) Then Return False
If MouseHit(1) And MouseY()>160 And MouseY()<177 Then
	If MouseX()>020 And MouseX()<100                Then 
		If (Not Test) And (Gelukt) Then Level = Level + 1
		Return True
		EndIf
	If MouseX()>100 And MouseX()<180 And (Not Test) And (Level<MaxLevels) Then SaveGame Gelukt
    If MouseX()>180 And MouseX()<260 And (Not Test)                       Then Return False
	If MouseX()>260 And MouseX()<340 And (Not Test) And (Joker>=ReqJoker) And (Not Gelukt) Then
		Score = Score - (ReqJoker*25)
		Joker = Joker - ReqJoker
		Level = Level + 1
		Return True
		EndIf
	EndIf
Forever
End Function

Function YesNo(Question$)
Local Ret = 3
SetFont Comic
Repeat
Cls
If KeyDown(1) Then Return False
DrawImage Buttons,200,200,7
DrawImage Buttons,400,200,8
MousePointer
Text 320,100,Question$,True,True
Flip
If MouseHit(1) And MouseY()>200 And MouseY()<217 Then
	If MouseX()>200 And MouseX()<280 Then Ret = 1
	If MouseX()>400 And MouseX()<480 Then Ret = 0
	EndIf
Until Not Ret=3
While MouseDown(1) Wend
FlushKeys
FlushMouse
Return Ret
End Function	

Function ObjectChooser()
Local Y = MouseY()
If MouseX()>560
	If Y>=020 And Y<=060 Then Obj = 1
	If Y>=080 And Y<=120 Then Obj = 2
	If Y>=140 And Y<=180 Then Obj = 3
	If Y>=200 And Y<=240 Then Obj = 4
	If Y>=462 And MouseDown(2) And (Not(Armageddon)) Then 
		Armageddon = True
		NewMessage "!!!ARMAGEDDON!!!"
		EndIf
	If Y>=445 And Y<=462 Then 
		FlushMouse
		PlayDrawScreen
		CenterImage SmallBox
		SetFont Comic
		Color 0,0,0
		Text 320,240,"Paused!",True,True
		Flip
		While Not(MouseHit(1)) Wend
		EndIf
	EndIf
End Function

Function SetCurrentObject()
If Veld(Level,CMX(),CMY()) = 0 Or Veld(Level,CMX(),CMY()) = 32 Or Veld(Level,CMX(),CMY()) = 33 Then
 Select Obj
	Case 1  If Plate1   (Level)>0 Then Veld(Level,CMX(),CMY()) = 32  Plate1   (Level) = Plate1   (Level) - 1
	Case 2  If Plate2   (Level)>0 Then Veld(Level,CMX(),CMY()) = 33  Plate2   (Level) = Plate2   (Level) - 1
	Case 3  If Wall     (Level)>0 Then Veld(Level,CMX(),CMY()) = 64  Wall     (Level) = Wall     (Level) - 1
	Case 4  If Bulldozer(Level)>0 Then Veld(Level,CMX(),CMY()) = 00  Bulldozer(Level) = Bulldozer(Level) - 1
	End Select
 End If
End Function

Function NewMessage(Message$)
Local Maximal = 480
For G.GameMessages = Each GameMessages
    If G\Y > Maximal Then MaxiMal=G\Y
	Next
GM.GameMessages = New GameMessages
GM\Message = Message
GM\Y = MaxiMal + 80
End Function

Function BreakAway(X,Y,Wind)
Local PX = X, PY = Y
Local APX,APY
Local Ret = False
Local Chan
Local More = False
Select Wind
	Case 1 PY = Y - 1
	Case 2 PY = Y + 1
	Case 3 PX = X + 1
	Case 4 PX = X - 1
	End Select
If Veld(Level,PX,PY)>79 And Veld(Level,PX,PY)<96 Then
    ;PlayDrawScreen
	;Flip
	SetBuffer FrontBuffer()
	Chan = PlaySound(Break)
	Score = Score + 1
	For Ak = 80 To 95
	    DrawImage ObjectImg,PX*40,PY*40,Ak
		Delay 10
		Next
	SetBuffer BackBuffer()
	Veld(Level,PX,PY) = 0
	Ret = True
	EndIf
;Ultimate Maddness
If LvlBack(Level) = 0 And Rand(1,4)=1 And Ret Then
	APX = Rand(1,10)
	APY = Rand(1,9)
	If Veld(Level,APX,APY) = 0 Then
		Veld(Level,APX,APY)=Rand(80,95)
		EndIf
	EndIf
;Are the more breakway blocks
For APX = 0 To 16 
	For APY = 0 To 11
		If Veld(Level,APX,APY) > 79 And Veld(Level,APX,APY)<96 Then More = True
		Next
	Next
If (Mission(Level) = 2) And (Not (More)) And (Ret) Then
	BallsIn = ObjBalls
	;RuntimeError "Balls In = "+BallsIn
	ObjBalls = 0
	EndIf
If (Mission(Level) = 3) And (Not (More)) And (Ret) Then
	Veld(Level,PX,PY)=112
	EndIf
;Okay it's all over now
Return Ret
End Function

Function Messenger()
SetFont Comic
Color 255,255,255
For GM.Gamemessages = Each GameMessages
	Text 320,GM\Y,GM\Message$,True
	GM\Y = GM\Y - 5
	If GM\Y < - 100 Then Delete GM
	Next
End Function

Function TypeName$(OType)
Local Ret$ = "Unknown Object"
Select OType
	Case 16   Ret$ = "Ball"
	Case 17   Ret$ = "Droid"
	Case 18   Ret$ = "Ghost"
	Case 19   Ret$ = "Green ball"
	Case 20   Ret$ = "Red ball"
	End Select
Return Ret$
End Function

Function Destroy(OType)
If OType=16 Or OType=19 Or OType=20
	Score = Score - 1
	ObjBalls = ObjBalls - 1
	BallsDestroyed = BallsDestroyed + 1
	EndIf
DelObject = True
End Function

Function CreateEnemyList()
Local X,Y
For X=0 To 16 For Y=0 To 11
	NME(X,Y) = 0
	Next Next
For O.Objects = Each Objects
	If O\OType<>16 And O\OType<>19 And O\OType<>20 Then 
		NME(O\X,O\Y)= O\OType
		;If Score = 0 Then DebugLog "Monster On ("+O\X+","+O\Y+") -> "+O\OType   ; Debug Line
		EndIf
	Next
;If Score=0 Then Score = 1   ;Debug Line	
End Function

Function Transport(X,Y,XY)
Local WX,WY
Local RX = X,RY = Y
For WX = 0 To 16
	For WY = 0 To 11
		If Veld(Level,WX,WY)=Veld(Level,X,Y) And (Not(WX=X And WY=Y)) Then 
			RX = WX
			RY = WY
			EndIf
		Next
	Next
If XY = 1 Then Return RX
Return RY
End Function

Function PlayLevel(Test)
MaskImage BackGround(0),255,255,255
If LvlBack(Level) = 0 Then
	SetBuffer ImageBuffer(BackGround(0))
	Cls
	SetBuffer BackBuffer()
	EndIf
BallsIn = 0
BallsDestroyed = 0
Repeat
Local Intro = IntroduceLevel(Test)
If Intro = 2 Then Return False
Until Intro = 1
Local Tr = Level
While (Tr>MaxCD) And (MaxCD<>0)
	Tr = Tr - MaxCD
	If KeyHit(1) Then RuntimeError "CD track initiatior doesn't work correctly, so it seems!"
	Wend
MaskImage BackGround(0),0,0,0
Local Timer = CreateTimer(5)
Local CDPlay = PlayCDTrack(Tr,2)
Obj = 1
Delete Each GameMessages
NewMessage "Good Luck"
Armageddon = False
Repeat
ScreenShot
If LvlBack(Level)=0 Then
	Color Rand(0,255),Rand(0,255),Rand(0,255)
	SetBuffer ImageBuffer(Background(0))
	Line Rand(0,639),Rand(0,479),Rand(0,639),Rand(0,479)
	Line Rand(0,639),Rand(0,479),Rand(0,639),Rand(0,479)
	Line Rand(0,639),Rand(0,479),Rand(0,639),Rand(0,479)
	Line Rand(0,639),Rand(0,479),Rand(0,639),Rand(0,479)
	Rect Rand(0,639),Rand(0,479),Rand(0,639),Rand(0,479),False
	Rect Rand(0,639),Rand(0,479),Rand(0,639),Rand(0,479),False
	Rect Rand(0,639),Rand(0,479),Rand(0,639),Rand(0,479),False
	Rect Rand(0,639),Rand(0,479),Rand(0,639),Rand(0,479),False	
	SetBuffer BackBuffer()
	EndIf
Blink = Blink + 255
If Blink > 255 Then Blink = 0 
PlayDrawScreen
MousePointer
Messenger
Flip
;Process the objects
For O.Objects = Each Objects
	DelObject = False
	;Let the objects move
	If Not(BlockFront(O\X,O\Y,O\Wind)) Then
		Select O\Wind
			Case 1  O\Y = O\Y - 1
			Case 2  O\Y = O\Y + 1
			Case 3  O\X = O\X + 1
			Case 4  O\X = O\X - 1
			End Select
		EndIf
	;Transporter
	If Veld(Level,O\X,O\Y) >= 48 And Veld(Level,O\X,O\Y) <= 63 Then
	    TX = O\X
		TY = O\Y
		O\X = Transport(TX,TY,1)
		O\Y = Transport(TX,TY,2)
		If SHowMouse Then NewMessage "Transported to ("+O\X+","+O\Y+")"
		EndIf		
	;Influence of plate /
	If (Veld(Level,O\X,O\Y) = 32 Or Veld(Level,O\X,O\Y) = 34) And O\OType <> 17 Then
		Select O\Wind
			Case 1  If Not Block(O\X+1,O\Y) Then O\Wind = 3
			Case 2  If Not block(O\X-1,O\Y) Then O\Wind = 4
			Case 3  If Not Block(O\X,O\Y-1) Then O\Wind = 1
			Case 4  If Not Block(O\X,O\Y+1) Then O\Wind = 2
			End Select
		End If
	;Influence of plate \
	If (Veld(Level,O\X,O\Y) = 33 Or Veld(Level,O\X,O\Y) = 35) And O\OType <> 17 Then
		Select O\Wind
			Case 1  If Not Block(O\X-1,O\Y) Then O\Wind = 4
			Case 2  If Not Block(O\X+1,O\Y) Then O\Wind = 3
			Case 3  If Not Block(O\X,O\Y+1) Then O\Wind = 2
			Case 4  If Not Block(O\X,O\Y-1) Then O\Wind = 1
			End Select
		End If
	;Arrows
	Select Veld(LEvel,O\X,O\Y)
		Case 37  O\Wind = 4
		Case 38  O\Wind = 3
		Case 39  O\Wind = 2
		Case 40  O\Wind = 1
		End Select
	;Objects can not go through walls
	If (BlockFront(O\X,O\Y,O\Wind)) Or (BreakAway(O\X,O\Y,O\Wind)) Then
		Select O\Wind
			Case 1  O\Wind = 2
			Case 2  O\Wind = 1
			Case 3  O\Wind = 4
			Case 4  O\Wind = 3
			End Select
		EndIf	
	;Normal Finish
	If Veld(Level,O\X,O\Y) = 112 And O\OType = 16
		ObjBalls = ObjBalls - 1
		BallsIn  = BallsIn  + 1
		Score    = Score    + 1
		DelObject = True
		PlaySound Good
		EndIf
	;Green Finish
	If Veld(Level,O\X,O\Y) = 113 Then
		If O\OType = 19 Then
			ObjBalls     = ObjBalls - 1
			BallsIn= BallsIn+ 1
			Score = Score + 1
			PlaySound Good
			DelObject = True
			EndIf
		If O\OType = 20 Then
		    Destroy O\OType
			NewMessage "Red Balls in Green Finish = DEATH!"
			EndIf
		EndIf
	;Red Finish
	If Veld(Level,O\X,O\Y) = 114 Then
		If O\OType = 20 Then
			ObjBalls     = ObjBalls - 1
			BallsIn= BallsIn + 1
			Score = Score + 1
			DelObject = True
			PlaySound Good
			EndIf
		If O\OType = 19 Then
		    Destroy O\OType
			NewMessage "Green Balls in Red Finish = DEATH!"
			EndIf
		EndIf
	;Water
	If Veld(Level,O\X,O\Y) = 96 Then
		Destroy O\OType
		NewMessage TypeName(O\OType)+" was lost in the water"
		EndIf
	;Mine
	If Veld(Level,O\X,O\Y) = 97 Then
		Destroy O\OType
		NewMessage TypeName(O\OType)+" exploded on a mine"
		Veld(Level,O\X,O\Y) = 0	
		PlaySound Explosion
		EndIf
	;Armageddon
	If Armageddon And Rand(1,10)=1 Then
		Score = Score - 2
		Destroy O\OType
	    O\OType = 6
		PlayDrawScreen
		Flip
		PlaySound Explosion
		Delay 50
		EndIf
	If DelObject Then Delete O
	Next
CreateEnemyList
For O.Objects = Each Objects
	DelObject = False	
	;Ball Hit By Enemy
	If (NME(O\X,O\Y)<>0) And (O\OType=16 Or O\OType=19 Or O\OType=20) Then
		Destroy O\OType
		NewMessage TypeName(O\OType)+" was destroyed by a "+TypeName(NME(O\X,O\Y))
		EndIf
	If DelObject Then Delete O
	Next
;When the mouse button is pushed
If MouseHit(1) Then
	ObjectChooser
	If MouseX()<560 Then SetCurrentObject
	EndIf	
WaitTimer Timer	
;If KeyHit(1) Then ObjBalls = 0
Until ObjBalls <= 0
FreeTimer Timer
StopChannel CDPlay
Return LevelOver(Test)
End Function



; A few Data Lines
.LevelEditorData
Data 0,9
Data 12,2,0,1,5,16,17,3,4

.Things
Data 7
Data 4,8,15,1,15,1,2

.PlayData
Data 446, 2
Data 10,14