$client = New-Object System.Net.Sockets.TcpClient('localhost', 6379)
$stream = $client.GetStream()
$writer = New-Object System.IO.StreamWriter($stream)
$writer.AutoFlush = $true
$reader = New-Object System.IO.StreamReader($stream)
$pingcommand = "*1`r`n`$4`r`nPING`r`n"
$writer.Write($pingcommand)
$response = $reader.ReadLine()
Write-Host "Server Response: $response"

$reader.Close()
$writer.Close()
$client.Close()
