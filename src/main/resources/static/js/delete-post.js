async function deleteFormHandler(event) {
    event.preventDefault();
    console.log("Deleting Post... ")
    const id = window.location.toString().split('/')[
    window.location.toString().split('/').length - 1
        ];

    const response = await fetch(`/api/posts/${id}`, {
        method: 'DELETE'
    });

    if (response.ok) {
        console.log("Post Deleted. Redirecting... ")
        document.location.replace('/dashboard')
    } else {
        alert(response.statusText);
    }
}

document.querySelector('.delete-post-btn').addEventListener('click', deleteFormHandler);