Set WshShell = WScript.CreateObject("WScript.Shell")

' Recupera gli argomenti passati allo script VBS
Dim args
Set args = WScript.Arguments

' Assicurati che siano stati passati tutti gli argomenti
If args.Count <> 4 Then
    WScript.Echo "Usage: connect_ssh.vbs [IP] [username] [password] [enterDelay]"
    WScript.Quit
End If

' Recupera gli argomenti
Dim ip, username, password, enterDelay
ip = args(0)
username = args(1)
password = args(2)
enterDelay = args(3)

' Esegui il comando SSH
WshShell.Run "cmd /c start ssh " & username & "@" & ip, 0, false

' Attendi prima di inviare la password
WScript.Sleep CInt(enterDelay)

' Invia la password utilizzando SendKeys
WshShell.SendKeys password
'WScript.Sleep 100
WshShell.SendKeys "{ENTER}"
