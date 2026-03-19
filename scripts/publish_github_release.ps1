param(
    [Parameter(Mandatory = $true)]
    [string]$GithubToken,
    [Parameter(Mandatory = $false)]
    [string]$Owner = "zerobun",
    [Parameter(Mandatory = $false)]
    [string]$Repo = "tailortech",
    [Parameter(Mandatory = $false)]
    [string]$Tag = "v1.0.0"
)

$ErrorActionPreference = "Stop"

$repoApi = "https://api.github.com/repos/$Owner/$Repo"
$releaseApi = "https://api.github.com/repos/$Owner/$Repo/releases"
$remoteUrl = "https://github.com/$Owner/$Repo.git"
$apkPath = "releases/v1.0.0/TailorTech-v1.0.0-debug.apk"
$zipPath = "releases/v1.0.0/TailorTech-v1.0.0-release-bundle.zip"

if (-not (Test-Path $apkPath)) { throw "Missing APK: $apkPath" }
if (-not (Test-Path $zipPath)) { throw "Missing zip bundle: $zipPath" }

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
    body = (Get-Content "RELEASE_NOTES_v1.0.0.md" -Raw)
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

function Upload-Asset([string]$filePath, [string]$name, [string]$contentType) {
    Write-Host "==> Uploading $name"
    $bytes = [System.IO.File]::ReadAllBytes((Resolve-Path $filePath))
    $uploadUrl = "$uploadBase?name=$name"
    Invoke-RestMethod -Method Post -Uri $uploadUrl -Headers @{
        Authorization = "Bearer $GithubToken"
        Accept = "application/vnd.github+json"
        "Content-Type" = $contentType
    } -Body $bytes | Out-Null
}

Upload-Asset -filePath $apkPath -name "TailorTech-v1.0.0-debug.apk" -contentType "application/vnd.android.package-archive"
Upload-Asset -filePath $zipPath -name "TailorTech-v1.0.0-release-bundle.zip" -contentType "application/zip"

Write-Host "==> Release published: https://github.com/$Owner/$Repo/releases/tag/$Tag"
