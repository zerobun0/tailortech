param(
    [Parameter(Mandatory = $true)]
    [string]$GithubToken,
    [Parameter(Mandatory = $false)]
    [string]$Owner = "zerobun0",
    [Parameter(Mandatory = $false)]
    [string]$Repo = "tailortech",
    [Parameter(Mandatory = $false)]
    [string]$Tag = "v1.0.4",
    [Parameter(Mandatory = $false)]
    [string]$ApkPath = ""
)

$ErrorActionPreference = "Stop"

$repoApi = "https://api.github.com/repos/$Owner/$Repo"
$releaseApi = "https://api.github.com/repos/$Owner/$Repo/releases"
$remoteUrl = "https://github.com/$Owner/$Repo.git"
$normalizedTag = $Tag.TrimStart('v', 'V')
if ([string]::IsNullOrWhiteSpace($ApkPath)) {
    $ApkPath = "releases/v$normalizedTag/TailorTech-v$normalizedTag-debug.apk"
}
$apkName = [System.IO.Path]::GetFileName($ApkPath)
$releaseNotesPath = "RELEASE_NOTES_${Tag}.md"

if (-not (Test-Path $ApkPath)) { throw "Missing APK: $ApkPath" }
if (-not (Test-Path $releaseNotesPath)) { $releaseNotesPath = "RELEASE_NOTES_v1.0.0.md" }

$headers = @{
    Authorization = "Bearer $GithubToken"
    Accept = "application/vnd.github+json"
    "X-GitHub-Api-Version" = "2022-11-28"
}

Write-Host "==> Checking repo existence $Owner/$Repo"
$repoExists = $true
try {
    Invoke-RestMethod -Method Get -Uri $repoApi -Headers $headers | Out-Null
} catch {
    $repoExists = $false
}

if (-not $repoExists) {
    Write-Host "==> Creating repo $Owner/$Repo"
    $createBody = @{ name = $Repo; private = $false; has_issues = $true; has_projects = $true; has_wiki = $false } | ConvertTo-Json
    Invoke-RestMethod -Method Post -Uri "https://api.github.com/user/repos" -Headers $headers -Body $createBody -ContentType "application/json" | Out-Null
}

Write-Host "==> Configuring git remote"
$hasOrigin = (git remote) -contains "origin"
if ($hasOrigin) {
    git remote set-url origin $remoteUrl
} else {
    git remote add origin $remoteUrl
}

Write-Host "==> Pushing code and tags"
git push -u origin master
git push origin --tags

Write-Host "==> Creating or reusing release $Tag"
$releaseBody = @{
    tag_name = $Tag
    name = "TailorTech $Tag"
    body = (Get-Content $releaseNotesPath -Raw)
    draft = $false
    prerelease = $false
} | ConvertTo-Json -Depth 4

$release = $null
try {
    $release = Invoke-RestMethod -Method Post -Uri $releaseApi -Headers $headers -Body $releaseBody -ContentType "application/json"
} catch {
    $release = Invoke-RestMethod -Method Get -Uri "$releaseApi/tags/$Tag" -Headers $headers
}

$uploadBase = $release.upload_url -replace "\{\?name,label\}", ""
$existingApk = $release.assets | Where-Object { $_.name -eq $apkName }
if ($existingApk) {
    Write-Host "==> Removing existing asset $apkName"
    $deleteApi = "https://api.github.com/repos/$Owner/$Repo/releases/assets/$($existingApk.id)"
    Invoke-RestMethod -Method Delete -Uri $deleteApi -Headers $headers | Out-Null
}

function Upload-Asset([string]$filePath, [string]$name, [string]$contentType) {
    Write-Host "==> Uploading $name"
    $bytes = [System.IO.File]::ReadAllBytes((Resolve-Path $filePath))
    $uploadUrl = "${uploadBase}?name=$name"
    Invoke-RestMethod -Method Post -Uri $uploadUrl -Headers @{
        Authorization = "Bearer $GithubToken"
        Accept = "application/vnd.github+json"
        "X-GitHub-Api-Version" = "2022-11-28"
        "Content-Type" = $contentType
    } -Body $bytes | Out-Null
}

Upload-Asset -filePath $ApkPath -name $apkName -contentType "application/vnd.android.package-archive"

Write-Host "==> Release published: https://github.com/$Owner/$Repo/releases/tag/$Tag"
