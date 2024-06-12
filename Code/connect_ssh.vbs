Set WshShell = WScript.CreateObject("WScript.Shell")

' Recupera gli argomenti passati allo script VBS
Dim args
Set args = WScript.Arguments

' Assicurati che siano stati passati tutti gli argomenti
If args.Count <> 3 Then
    WScript.Echo "Usage: connect_ssh.vbs [IP] [username] [password]"
    WScript.Quit
End If

' Recupera gli argomenti
Dim ip, username, password
ip = args(0)
username = args(1)
password = args(2)

' Esegui il comando SSH
WshShell.Run "cmd /c start ssh " & username & "@" & ip, 0, false

' Attendi un secondo prima di inviare la password
WScript.Sleep 500

' Invia la password utilizzando SendKeys
WshShell.SendKeys password
'WScript.Sleep 100
WshShell.SendKeys "{ENTER}"

